## PubSub Networing Assignment ##
a publish and subscribe networking assignment to implement a Hard Coded MQTT protocol over UDP. 

The topology consists of:
    - A Client publishing information to a Broker 
    - A Broker which sends and receives information to/from different sources
    - A Server which subscribes to information published by Clients and Publishes instructions to Subscribers via the Broker
    - A Subscriber which subscribe to instructions published by Server

The topology is implemented via the use of Docker containers on my local machine.

For information on Setup, Please see SETUP.md in /PubSub/src 

