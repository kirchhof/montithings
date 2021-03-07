/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
// (c) https://github.com/MontiCore/monticore
#include "MessageFlowRecorder.h"

// ignore warnings about endless loops
#pragma clang diagnostic push
#pragma ide diagnostic ignored "EndlessLoop"

void
MessageFlowRecorder::init ()
{
  ddsCommunicator.initConfig ();
  ddsCommunicator.initParticipant ();
  ddsCommunicator.initMessageTypes ();
  ddsCommunicator.initTopics ();
  ddsCommunicator.initSubscriber ();
  ddsCommunicator.initPublisher ();
  ddsCommunicator.initWriter ();
  ddsCommunicator.initReaderRecorderMessage ();
  ddsCommunicator.initReaderCommandReplyMessage ();
  ddsCommunicator.initReaderAcknowledgement (true); // todo check this

  ddsCommunicator.setPortIdentifier ("recorder");

  ddsCommunicator.addOnRecorderMessageCallback (
      std::bind (&MessageFlowRecorder::onDebugMessage, this, std::placeholders::_1));
  ddsCommunicator.addOnCommandReplyMessageCallback (
      std::bind (&MessageFlowRecorder::onCommandReplyMessage, this, std::placeholders::_1));
  ddsCommunicator.addOnAcknowledgementMessageCallback (
      std::bind (&MessageFlowRecorder::onAcknowledgementMessage, this, std::placeholders::_1));
}

void
MessageFlowRecorder::setFileRecordings (std::string &filePath)
{
  this->fileRecordingsPath = filePath;
}

void
MessageFlowRecorder::setDcpsInfoRepoHost (std::string &host)
{
  ddsCommunicator.setDcpsInfoRepoHost (host);
}

void
MessageFlowRecorder::setVerbose (bool verbose)
{
  ddsCommunicator.setVerbose (verbose);
}

void
MessageFlowRecorder::setInstanceAmount (int n)
{
  instanceAmount = n;
}

void
MessageFlowRecorder::start ()
{
  recordStorage = nlohmann::json::object ();
  statsCallsAmount = 0;
  statsLatenciesAmount = 0;

  LOG_F (INFO, "Waiting until application is started... ");
  ddsCommunicator.waitUntilCommandReadersConnected (instanceAmount);
  if (instanceAmount == 1)
    {
      LOG_F (INFO, "At least one entity listens for commands. Waiting 2 seconds for others.");
      std::this_thread::sleep_for (std::chrono::seconds (2));
      LOG_F (INFO, "We can probably start.");
    }
  else
    {
      LOG_F (INFO, "%d entities listen for commands. We can start.", instanceAmount);
    }

  // starts thread which logs the current amount of recorded data each 5 seconds
  std::thread logger (&MessageFlowRecorder::logProgress, this);
  logger.detach ();

  LOG_F (INFO, "Sending command: RECORDING_START");
  DDSRecorderMessage::Command startCommand;
  startCommand.id = Util::Time::getCurrentTimestampUnix ();
  startCommand.cmd = DDSRecorderMessage::RECORDING_START;
  ddsCommunicator.send (startCommand);

  LOG_F (INFO, "Waiting until all participants acknowledge the RECORDING_START command...");
  if (!ddsCommunicator.commandWaitForAcks ())
    {
      LOG_F (ERROR, "RECORDING_START command was not ACKed by all components, stopping.");
      stop ();
    }
  LOG_F (INFO, "RECORDING_START command was ACKed by participants.");

  LOG_F (INFO, "Recording...");
  isRecording = true;

  // recording is done event-based. However, the program should not exit at this point, hence the
  // endless loop.
  while (true)
    {
    }
}

void
MessageFlowRecorder::stop ()
{
  LOG_F (INFO, "Stopping...");
  if (!isRecording)
    {
      LOG_F (INFO, "Recording did not start, yet. There is nothing to do, exiting...");
      cleanup ();
      exit (EXIT_SUCCESS);
    }

  isRecording = false;
  LOG_SCOPE_F (INFO, "Sending command: RECORDING_STOP");
  DDSRecorderMessage::Command stopCommand;
  stopCommand.id = Util::Time::getCurrentTimestampUnix ();
  stopCommand.cmd = DDSRecorderMessage::RECORDING_STOP;
  ddsCommunicator.send (stopCommand);

  LOG_F (INFO, "Waiting for ACKs...");
  if (!ddsCommunicator.commandWaitForAcks ())
    {
      LOG_F (ERROR, "RECORDING_STOP command was not ACKed by all components, exiting.");
      cleanup ();
      exit (1);
    }
  LOG_F (INFO, "RECORDING_STOP command was ACKed by all subscribers.");

  LOG_F (INFO, "Waiting until all participants disconnect...");
  ddsCommunicator.waitUntilRecorderWritersDisconnect ();
  LOG_F (INFO, "All participants disconnected.");
}

void
MessageFlowRecorder::process ()
{
  RecordProcessor processor;
  if (!recordedMessages.empty ())
    {
      recordStorage["recordings"] = processor.process (recordedMessages);
    }
}

void
MessageFlowRecorder::cleanup ()
{
  ddsCommunicator.cleanup ();
}

void
MessageFlowRecorder::saveToFile ()
{
  LOG_F (INFO, "Flushing recorded data into file %s.", fileRecordingsPath.c_str ());
  std::ofstream outfile;
  outfile.open (fileRecordingsPath);
  outfile << recordStorage.dump ();
  outfile.close ();
}

void
MessageFlowRecorder::onDebugMessage (const DDSRecorderMessage::Message &message)
{
  LOG_F (1, "onDebugMessage:%d,%s,%s,%d,%ld,%s,%s.", message.id, message.instance_name.in (),
         message.msg_content.in (), message.msg_id, message.timestamp, message.topic.in (),
         message.message_delays.in ());

  switch (message.type)
    {
    case DDSRecorderMessage::MESSAGE_RECORD:
      ddsCommunicator.sendAck (message.instance_name.in (), message.id, "recorder", "");

      // store message unprocessed and move on, dont wae time
      recordedMessages.push_back (message);
      break;
    case DDSRecorderMessage::INTERNAL_RECORDS:
      {
        nlohmann::json content = nlohmann::json::parse (message.msg_content.in ());
        std::string instance = message.instance_name.in ();

        for (auto call : content["calls"])
          {
            std::string callId = call[0].dump ();
            std::string value = call[1]["v"].dump ();

            recordStorage["calls"][instance][callId] = value;
            statsCallsAmount++;
          }

        for (auto latency : content["calc_latency"])
          {
            std::string latencyId = latency[0].dump ();
            recordStorage["computation_latency"][instance][latencyId] = latency[1];
            statsLatenciesAmount++;
          }

        LOG_F (1,
               "Received internal recording data from %s: %ld system calls intercepted and %ld "
               "computation latencies measured.",
               instance.c_str (), content["calls"].size (), content["calc_latency"].size ());
        break;
      }
    default:
      std::cerr << "DDSRecorder | onDebugMessage: unknown message type" << std::endl;
    }
}

void
MessageFlowRecorder::onCommandReplyMessage (const DDSRecorderMessage::CommandReply &message)
{
  LOG_F (1, "onCommandReplyMessage:%d,%s,%d.", message.id, message.content.in (),
         message.command_id);
}

void
MessageFlowRecorder::onAcknowledgementMessage (const DDSRecorderMessage::Acknowledgement &ack)
{
  LOG_F (1, "onAcknowledgementMessage:%d,%s,%s,%d,%s.", ack.id, ack.sending_instance.in (), ack.sending_instance.in(),ack.acked_id,
         ack.serialized_vector_clock.in ());
}

void
MessageFlowRecorder::logProgress ()
{
  while (true)
    {
      std::this_thread::sleep_for (std::chrono::seconds (5));

      if (isRecording)
        {
          LOG_F (INFO, "Messages: %lu, Calls: %u, Computation Latencies: %u",
                 recordedMessages.size (), statsCallsAmount, statsLatenciesAmount);
        }
    }
}

#pragma clang diagnostic pop