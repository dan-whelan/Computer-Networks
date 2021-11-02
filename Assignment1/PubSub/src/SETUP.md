# A guide to setting up the topology for CSU33031 Assignment 1 using Docker (M1 macbook running BigSur)
>terminal 1
docker network create -d bridge --subnet 172.20.0.0/16 PubSub
docker create --name client -ti -v $PWD/PubSub:/PubSub arm64v8/openjdk /bin/bash
docker create --name broker -ti -v $PWD/PubSub:/PubSub arm64v8/openjdk /bin/bash
docker create --name server -ti -v $PWD/PubSub:/PubSub arm64v8/openjdk /bin/bash
docker create --name subscriber -ti -v $PWD/PubSub:/PubSub arm64v8/openjdk /bin/bash

docker network connect PubSub client
docker network connect PubSub broker
docker network connect PubSub server
docker network connect PubSub subscriber

docker start -i broker

cd PubSub/src 

javac -cp . *.java
java -cp . Broker

>terminal 2
docker start -i server

cd PubSub/src

java -cp . Server

>terminal 3
docker start -i subscriber

cd PubSub/src

java -cp . Subscriber

>terminal 4
docker start -i client

cd PubSub/src

java -cp . Client

>To Set Up Wireshark
>terminal 1
# taken from stack overflow "https://stackoverflow.com/questions/61232668/runing-openmodelica-gui-from-docker-causes-could-not-connect-to-any-x-display"
docker-machine env default
eval $(docker-machine env default)
IP=$(ifconfig en0 | grep inet | awk '$1=="inet" {print $2}')
xhost + $IP

socat TCP-LISTEN:6000,reuseaddr,fork UNIX-CLIENT:\"$DISPLAY\"

>terminal 2
docker create --name wireshark -ti -v $PWD/PubSub:/config --cap-add:ALL --privileged DISPLAY=docker.for.mac.localhost:0 --net host ubuntu:latest /bin/bash
docker start -i wireshark

apt-get update && \
apt-get install -y wireshark && \
apt-get install -y --no-install-recommends binutils && \
ldconfig -p | grep libQt5Core.so.5 | cut -d '>' -f 2 | xargs -I{} strip --remove-section=.note.ABI-tag {} && \
apt-get remove -y --purge binutils && \
apt-get autoremove -y --purge && \
rm -rf /var/lib/apt/lists/*
setcap setcap 'CAP_NET_RAW+eip CAP_NET_ADMIN+eip' /usr/bin/dumpcap

wireshark