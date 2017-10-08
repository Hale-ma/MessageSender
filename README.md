# MessageSender
UCD Project:Android Message Diffusion Over Link Layer Protocols 
#Project Specification

**Subject**NetWorks

**Project Type**:Design and Implementation

**Software Requirements**Java, Android Studio

**Hardware Requirements**PC for developing, Android smartphone for testing/using

Nowadays, mobile phones normally have several wireless interfaces including Bluetooth, Wi-Fi, which are useable in ad-hoc mode. The aim of this project is to build an Android application to send messages directly without using the TCP/IP stack. A simple protocol should be designed above link layer. The user of application can send message with a network identifier. Any node receiving the message will check the device identifier and will forward or drop the message depending on the identifier. To avoid infinite retransmissions, messages are cached for some time so that any node receiving a previously transmitted message (i.e., cached) will drop it.

The mandatory part of this project is a working implementation of one broadcasting protocol over one layer two technology and a basic evaluation of the application in terms of bandwidth and delay., which is Wi-Fi in this project. The discretionary part of this project is Implementation and usage of more than one link layer technology. For example, a node receiving a message over Wi-Fi, could then forward it over Bluetooth.The expectational part of this project is an evaluation of the application in terms of bandwidth and delay in a multi-hop scenario were messages go from the source to the destination through one or more relay.

# Conclusions and Future Work
This project implements an Android application that builds a decentralised mobile ad-hoc network. The application can discover nearby nodes, and send message to any node in the network. the performance and the reliability are enhanced with using two wireless interfaces and the supporting routing algorithm. 

Review the entire project development process, there are two key decisions. The first key decision is deciding the responsibility for each Wireless interface (section 3.1). Using Wi-Fi service discovery to broadcast rather than use Wi-Fi Direct connect for Wi-Fi interface and Bluetooth connection is used to send peer to peer message. This decision is based on the performance testing on each wireless interface. The second key decision is to combine flooding routing and link-state routing as the routing algorithm for the project and give up location-based greed routing (Section 3.2). This decision is based on the performance of each wireless interface and the characteristics of mobile networks.

Compairing to existing similar project, this project has following features: Unlike wireless mesh network, this project can build with only by mobile phone without using other any infrastructure. This project using two wireless interfaces at same time, which provides an algorithm that coordinates two wireless interfaces.The application used origin Android API, which does not need to root the phone or become the privileged user to install the application.

This application has the following shortcomings: The discovery step is done by Wi-Fi interface by service discovery when a new node join the network. Because the delay of Wi-Fi service discovery is high, the discovery may take up to a minute to find the nearby device; To keep Wi-Fi service discovery working, the application will constantly switch the Wi-Fi interface. This makes cause lag and affects other application that using Wi-Fi. What is more, there is vulnerability of security of the message. Because the Wi-Fi direct service discovery is broadcast the Plaintext without encryption, the message may be eavesdropped. Man-in-the-middle attack is also possible.

There are some features considered to add to the current application. Sending file is a useful feature in modern chatting applications. In this project, Bluetooth interface can be used to send file; Enable user to customise their username and avatar can enhance user experience. This feature requires to sending extra message when discovering node. 


