# Sliding Work Sharing 

Sliding Work Sharing Management (SWS) Component of the AI4Work Project.  

## Prerequisites
To build and run the Sliding Work Sharing, the following software is required:
- **Java**: Version 23.
- **Apache Maven**: Version 3.8.5 or later.

---

## Installation and Setup

### 1. Install Dependencies and Build the Application
Open a terminal in the project directory and execute the following command to clean previous build artifacts, install dependencies and build the application:

```bash
mvn clean install
```

### 2. Start the Application
Run the following command to start the application (it will start the default 'logistics' scenario):

```bash
mvn spring-boot:run
```

The application will start and listen on port `8080` by default.

---

## How to Test the Application

You can test the application using the `curl` command (or using any other HTTP/REST client of your choice):

### Example Request
Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the `/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "noOfTrucksInQueue": 7,
      "positionOfTruckToBePrioritized": 5
    }
  }'

```

### Example Response
The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "decisionResult": {
    "slidingDecision": "HUMAN_ON_THE_LOOP",
    "description": "Human has to be informed about AI's rescheduling"
  }
}
```

---

## Demonstration Scenarios

The scenarios available in the application are:
1. [Logistics Scenario](#logistics-scenario)
2. [Agriculture Scenario](#agriculture-scenario)

### Logistics Scenario

This first demo version is based on a simplified demonstration example:

- imagine a queue of trucks that deliver material to a warehouse
- usually the sequence of trucks is "first come, first served"
- an exception to the previous rule is that a truck should be prioritized if
  - the stock of some material is getting low
  - a truck is delivering that very same material

The SWS management decides in how far a human should be involved in the decision. This decision depends on
- the length of the queue
- the position of the truck in the queue

### Example Rules

- if the queue is short and the truck is near its front, it can be automatically prioritized
- if the queue is long and the truck is near its end, a human needs to be involved in the decision

#### How to Run the Scenario
To start the application and run the logistics scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=logistics
```

#### Testing the Logistics Scenario

#### Example Request
Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the `/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "noOfTrucksInQueue": 7,
      "positionOfTruckToBePrioritized": 5
    }
  }'
```

### Example Response
The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "decisionResult": {
    "slidingDecision": "HUMAN_ON_THE_LOOP",
    "description": "Human has to be informed about AI's rescheduling"
  }
}
```

To test the implemented rules, you can pass different input parameters to the application and observe the outcomes. Just modify the values for the `slidingDecisionInputParameters` in the POST request as mentioned below
- `noOfTrucksInQueue`: Total number of trucks in the queue (0-20)
- `positionOfTruckToBePrioritized`: Position of the truck that needs to be prioritization (0-20)

### Agriculture Scenario

This example is from the agriculture/horticulture domain:

- imagine workers who are manually harvesting produce and putting it into boxes
- whenever a box is full of produce, it has to be carried to a central collection point
- carrying the box can be done either by the workers themselves or by an AI-powered transport drone
- the transport drone has limited capacity, so it cannot transport all boxes for all workers

For each required transport of a harvest box, the SWS management decides in how far a human (either worker or supervisor) should be involved in the decision if this transport should be done by the drone or by the worker. This decision depends on:

- the current waiting time for the drone (waiting time is in minutes)
- the fatigue level of the worker (fatigue level is in percentage)
- the distance from the current location of the box to the central collection point (distance in the meters)

#### Example rules

The following is a first draft of the rules to be defined in an FCL file. Later we can later improve the rules by adjusting the FCL file.

- if the distance is low, let the worker carry the box
- if the distance is high or the waiting time for the drone is low, let the drone carry the box
- if the waiting time for the drone is high and the fatigue level of the worker is low, let the worker carry the box
- if the waiting time for the drone is high and the distance is high and the fatigue level of the worker is low, then let the worker decide (if or if not to wait for the drone)
- if the waiting time for the drone is high and the fatigue level of the worker is high, let the drone carry the box, but inform the supervisor (who may intervene and decide to not wait for the drone)

For the time being, the potential outputs should be the same as for the transport/logistics scenario. A rough mapping to the existing

- let the worker carry the box: "human manually"
- let the drone carry the box: "ai autonomously"
- let the worker decide: "human in the loop"
- inform supervisor, who may potentially intervene: "human on the loop"

#### How to Run the Scenario
To start the application and run the Agriculture scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=agriculture
```

#### Testing the Agriculture Scenario

#### Example Request
Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the `/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "distanceToCentralCollectionPoint": 10,
      "waitingTimeForDrone": 5,
      "fatigueLevelOfWorker": 18
    }
}'
```

### Example Response
The application will respond with a JSON string similar to the following:

```json
{
    "decisionStatus": "Sliding Decision Response",
    "decisionResult": {
        "slidingDecision": "AI_AUTONOMOUSLY",
        "description": "Let the drone carry the box"
    }
}
```

To test the implemented rules, you can pass different input parameters to the application and observe the outcomes. Just modify the values for the `slidingDecisionInputParameters` in the POST request as mentioned below
- `distanceToCentralCollectionPoint`: Distance to collection point (0-20)
- `waitingTimeForDrone`: Time until drone becomes available (0-20)
- `fatigueLevelOfWorker`: Current fatigue level of the worker (0-20)
