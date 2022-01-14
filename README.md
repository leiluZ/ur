# ur
Introduction

# Prepare Environment
> OS: ubuntu-20.04.3-desktop  

> sudo apt install openssh-server  
> sudo apt install git  
> sudo apt install openjdk-11-jre-headless  
> sudo apt install openjdk-11-jdk-headless  
> sudo apt install maven  
> sudo apt install net-tools  
> sudo apt install tree  
> sudo apt install python-is-python3  
> sudo snap install intellij-idea-community --classic  

# Repository Structure

This repository includes 3 parts: (`materials`, `saDemo`, and `exercises`)

ur@ur-VirtualBox:~$ tree -L 4 ur/  
ur/  
├── exercises  
│   ├── class4  
│   │   ├── nyc_taxi.json  
│   │   ├── PravegaSamples  
│   │   └── README.md  
│   ├── class6  
│   │   └── README.md  
│   ├── class7  
│   │   └── README.md  
│   └── finalExam  
│       └── README.md  
├── materials  
│   ├── install  
│   │   ├── flink-1.13.3  
│   │   ├── pravega-0.10.1  
│   │   ├── pravega-connectors-flink-1.13_2.12-0.10.1.jar  
│   │   └── README.md  
│   └── source_code  
│       ├── flink_related  
│       │   ├── flink  
│       │   ├── flink-training  
│       │   └── README.md  
│       └── pravega_related  
│           ├── flink-connectors  
│           ├── pravega  
│           ├── pravega-samples  
│           └── README.md  
├── README.md  
└── saDemo  
    └── README.md  


## materials
Contains all used source code and installation distributions for used open source projects

## saDemo
Contains demo project for use case

## exercises
Contains exercises for each class and final examination
