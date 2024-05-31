FROM ubuntu

# install prerequisites
RUN apt-get update
RUN apt-get -y install git
RUN apt -y install sudo
RUN sudo apt-get -y install curl

# set file encoding correctly
RUN sudo apt -y install locales && locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8


# install montithings
RUN git clone https://github.com/MontiCore/montithings.git
WORKDIR "/montithings"
RUN sudo apt-get update
RUN sudo apt-get install -y software-properties-common
RUN sudo add-apt-repository -y ppa:openjdk-r/ppa
RUN sudo apt-get install -y g++ git make cmake ninja-build mosquitto-dev libmosquitto-dev curl maven \
  openjdk-11-jdk 
RUN sudo apt-get install -y python3 
RUN sudo apt-get install -y python3-pip 
RUN sudo apt-get install -y mosquitto-clients 
RUN sudo apt-get install -y libssl-dev libpq-dev
RUN sudo apt-get install -y protobuf-compiler libprotobuf-dev 
RUN sudo apt-get install -y python3-protobuf
RUN sudo apt-get install -y python3-paho-mqtt || pip3 install paho-mqtt

RUN mvn clean install -Dexec.skip -Dmaven.test.skip=true
