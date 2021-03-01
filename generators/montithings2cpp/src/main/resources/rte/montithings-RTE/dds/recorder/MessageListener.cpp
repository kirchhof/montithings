/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
// (c) https://github.com/MontiCore/monticore
#include "MessageListener.h"

#include <utility>

void
MessageListener::on_data_available (DDS::DataReader_ptr reader)
{
  DDS::SampleInfo info{};
  DDS::ReturnCode_t error;

  // std::cout << "ondata topic: " << reader->get_topicdescription()->get_type_name() << std::endl;
  if (strcmp (reader->get_topicdescription ()->get_type_name (), RECORDER_MESSAGE_TYPE) == 0)
    {
      DDSRecorderMessage::MessageDataReader_var reader_i
          = DDSRecorderMessage::MessageDataReader::_narrow (reader);

      if (!reader_i)
        {
          std::cerr << "MessageListener | on_data_available _narrow DDSRecorderMessage failed!"
                    << std::endl;
          exit (EXIT_FAILURE);
        }

      DDSRecorderMessage::Message message;
      error = reader_i->take_next_sample (message, info);

      if (error == DDS::RETCODE_OK && info.valid_data)
        {
          on_data_available (message);
        }
      else
        {
          // std::cerr << "MessageListener | on_data_available take_next_sample failed!" <<
          // std::endl;
        }
    }
  else if (strcmp (reader->get_topicdescription ()->get_type_name (), RECORDER_COMMAND_TYPE) == 0)
    {
      DDSRecorderMessage::CommandDataReader_var reader_i
          = DDSRecorderMessage::CommandDataReader::_narrow (reader);

      if (!reader_i)
        {
          std::cerr << "MessageListener | on_data_available _narrow DDSCommandMessage failed!"
                    << std::endl;
          exit (EXIT_FAILURE);
        }

      DDSRecorderMessage::Command message;
      error = reader_i->take_next_sample (message, info);

      if (error == DDS::RETCODE_OK && info.valid_data)
        {
          on_data_available (message);
        }
      else
        {
          // std::cerr << "MessageListener | on_data_available take_next_sample failed!" <<
          // std::endl;
        }
    }
  else if (strcmp (reader->get_topicdescription ()->get_type_name (), RECORDER_COMMANDREPLY_TYPE)
           == 0)
    {
      DDSRecorderMessage::CommandReplyDataReader_var reader_i
          = DDSRecorderMessage::CommandReplyDataReader::_narrow (reader);

      if (!reader_i)
        {
          std::cerr << "MessageListener | on_data_available _narrow DDSCommandMessage failed!"
                    << std::endl;
          exit (EXIT_FAILURE);
        }

      DDSRecorderMessage::CommandReply message;
      error = reader_i->take_next_sample (message, info);

      if (error == DDS::RETCODE_OK && info.valid_data)
        {
          on_data_available (message);
        }
      else
        {
          // std::cerr << "MessageListener | on_data_available take_next_sample failed!" <<
          // std::endl;
        }
    }
  else if (strcmp (reader->get_topicdescription ()->get_type_name (), RECORDER_ACKNOWLEDGE_TYPE)
           == 0)
    {
      DDSRecorderMessage::AcknowledgementDataReader_var reader_i
          = DDSRecorderMessage::AcknowledgementDataReader::_narrow (reader);

      if (!reader_i)
        {
          std::cerr << "MessageListener | on_data_available _narrow AcknowledgementMessage failed!"
                    << std::endl;
          exit (EXIT_FAILURE);
        }

      DDSRecorderMessage::Acknowledgement message;
      error = reader_i->take_next_sample (message, info);

      if (error == DDS::RETCODE_OK && info.valid_data)
        {
          on_data_available (message);
        }
      else
        {
          // std::cerr << "MessageListener | on_data_available take_next_sample failed!" <<
          // std::endl;
        }
    }
}

void
MessageListener::addOnRecorderMessageCallback (
    std::function<void (DDSRecorderMessage::Message)> callback)
{
  onRecorderMessageCallback = std::move(callback);
}

void
MessageListener::addOnCommandMessageCallback (
    std::function<void (DDSRecorderMessage::Command)> callback)
{
  onCommandMessageCallback = std::move(callback);
}

void
MessageListener::addOnCommandReplyMessageCallback (
    std::function<void (DDSRecorderMessage::CommandReply)> callback)
{
  onCommandReplyMessageCallback = std::move(callback);
}

void
MessageListener::addOnAcknowledgementMessageCallback (
    std::function<void (DDSRecorderMessage::Acknowledgement)> callback)
{
  onAcknowledgementMessageCallback = std::move(callback);
}

void
MessageListener::on_data_available (const DDSRecorderMessage::Message& message)
{
  if (isVerbose)
    {
      std::cout << message.id << " | " << message.msg_content << " | " << message.timestamp << " | "
                << message.topic << std::endl;
    }
  if (onRecorderMessageCallback)
    {
      onRecorderMessageCallback (message);
    }
}

void
MessageListener::on_data_available (const DDSRecorderMessage::Command& message)
{
  if (isVerbose)
    {
      std::cout << message.id << " | " << message.cmd << std::endl;
    }
  if (onCommandMessageCallback)
    {
      onCommandMessageCallback (message);
    }
}

void
MessageListener::on_data_available (const DDSRecorderMessage::CommandReply& message)
{
  if (isVerbose)
    {
      std::cout << message.id << " | " << message.content << " | " << message.command_id
                << std::endl;
    }
  if (onCommandReplyMessageCallback)
    {
      onCommandReplyMessageCallback (message);
    }
}

void
MessageListener::on_data_available (const DDSRecorderMessage::Acknowledgement& message)
{
  if (isVerbose)
    {
      std::cout << message.id << " | " << message.instance << " | " << message.acked_id
                << std::endl;
    }
  if (onAcknowledgementMessageCallback)
    {
      onAcknowledgementMessageCallback (message);
    }
}

/*
 * Unused methods
 */

void
MessageListener::on_requested_deadline_missed (
    DDS::DataReader_ptr /*reader*/, const DDS::RequestedDeadlineMissedStatus & /*status*/)
{
}

void
MessageListener::on_requested_incompatible_qos (
    DDS::DataReader_ptr /*reader*/, const DDS::RequestedIncompatibleQosStatus & /*status*/)
{
}

void
MessageListener::on_sample_rejected (DDS::DataReader_ptr /*reader*/,
                                     const DDS::SampleRejectedStatus & /*status*/)
{
}

void
MessageListener::on_liveliness_changed (DDS::DataReader_ptr /*reader*/,
                                        const DDS::LivelinessChangedStatus & /*status*/)
{
}

void
MessageListener::on_subscription_matched (DDS::DataReader_ptr reader,
                                          const DDS::SubscriptionMatchedStatus &status)
{
  // std::cout << "Change detected on topic " << reader->get_topicdescription()->get_name() << ".
  // Todal amount of writers: " <<  status.current_count << std::endl;
}

void
MessageListener::on_sample_lost (DDS::DataReader_ptr /*reader*/,
                                 const DDS::SampleLostStatus & /*status*/)
{
}