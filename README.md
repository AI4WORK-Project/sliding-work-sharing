# Sliding Work Sharing 

Sliding Work Sharing (SWS) Management Component of the AI4Work Project.  

The aim of this software is to support dynamic work sharing between human and machine (especially AI/robots). It can provide decision support regarding the appropriate degree of machine autonomy and the required degree of human involvement, depending on the respective work situation. 

---

## How to Build and Run the Application

### 1. Prerequisites

To build and run the Sliding Work Sharing, the following software is required:
- **Java**: Version 23.
- **Apache Maven**: Version 3.8.5 or later.

### 2. Install Dependencies and Build the Application

Open a terminal in the project directory and execute the following command to clean previous build artifacts, install dependencies and build the application:

```bash
mvn clean install
```

### 3. Start the Application

Run the following command to start the application:

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
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```
_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is described [here](#how-to-read-the-decisionexplanation).  

---

## Demonstration Scenarios

To make this software useful for application in different domains, it can be configured via application-scenario-specific rules. This is explained in the following, based on simplified example scenarios from the AI4Work project's pilot domains:

1. [Logistics Scenario](#logistics-scenario)
2. [Agriculture Scenario](#agriculture-scenario)
3. [Construction Scenario](#construction-scenario)

### Logistics Scenario

This example scenario is from the logistics domain:

- imagine a queue of trucks that deliver material to a warehouse
- usually the sequence of trucks is "first come, first served"
- an exception to the previous rule is that a truck should be prioritized if
  - the stock of some material is getting low
  - a truck is delivering that very same material

The SWS management decides in how far a human should be involved in the decision. This decision depends on:

- the length of the queue
- the position of the truck in the queue

#### Example Rules

- if the queue is short and the truck is near its front, it can be automatically prioritized
- if the queue is long and the truck is near its end, a human needs to be involved in the decision

To explore the example rules in detail, please refer to the FCL file located [here](src/main/resources/rules/TruckSchedulingSlidingDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the logistics scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=logistics
```

##### Example Request

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

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify the values for the `slidingDecisionInputParameters` as follows:
- `noOfTrucksInQueue`: Total number of trucks in the queue (0-20 trucks)
- `positionOfTruckToBePrioritized`: Position of the truck number that needs to be prioritization (0-20)

##### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "decisionResult": {
    "slidingDecision": "HUMAN_ON_THE_LOOP",
    "description": "Human has to be informed about AI's rescheduling"
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```
_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is described [here](#how-to-read-the-decisionexplanation).

Depending on the input, the SWS may decide one of the following:
- `AI_AUTONOMOUSLY`: "AI can reschedule without human involvement"
- `HUMAN_ON_THE_LOOP`: "Human has to be informed about AI's rescheduling"
- `HUMAN_IN_THE_LOOP`: "Human has to check AI's suggestion"
- `HUMAN_MANUALLY`: "Human has to decide without AI support"

---

### Agriculture Scenario

This example is from the agriculture domain:

- imagine workers who are manually harvesting produce and putting it into boxes
- whenever a box is full of produce, it has to be carried to a central collection point
- carrying the box can be done either by the workers themselves or by an AI-powered transport drone
- the transport drone has limited capacity, so it cannot transport all boxes for all workers

For each required transport of a harvest box, the SWS management decides in how far a human (either worker or supervisor) should be involved in the decision if this transport should be done by the drone or by the worker. This decision depends on:

- the current waiting time for the drone
- the fatigue level of the worker 
- the distance from the current location of the box to the central collection point

#### Example rules

- if the distance is low, let the worker carry the box
- if the distance is high or the waiting time for the drone is low, let the drone carry the box
- if the waiting time for the drone is high and the fatigue level of the worker is low, let the worker carry the box

To explore the example rules in detail, please refer to the FCL file located [here](src/main/resources/rules/AgricultureSchedulingSlidingDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the agriculture scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=agriculture
```

##### Example Request

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the `/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "distanceToCentralCollectionPoint": 250,
      "waitingTimeForDrone": 2,
      "fatigueLevelOfWorker": 80
    }
}'
```

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify the values for the `slidingDecisionInputParameters` as follows:
- `distanceToCentralCollectionPoint`: The distance to the collection point, measured in meters (0-300 meters)
- `waitingTimeForDrone`: The time until the drone becomes available, measured in minutes (0-15 minutes)
- `fatigueLevelOfWorker`: The current fatigue level of the worker, measured in percent (0%-100%)

##### Example Response
The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "decisionResult": {
    "slidingDecision": "AI_AUTONOMOUSLY",
    "description": "Let the drone carry the box"
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```
_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is described [here](#how-to-read-the-decisionexplanation).

Depending on the input parameters, the SWS may decide one of the following:
- `AI_AUTONOMOUSLY`: "Let the drone carry the box"
- `HUMAN_ON_THE_LOOP`: "Inform supervisor, who may potentially intervene"
- `HUMAN_IN_THE_LOOP`: "Let the worker decide"
- `HUMAN_MANUALLY`: "Let the worker carry the box"

---

### Construction Scenario

This example scenario is from the construction domain:

- imagine a robot doing work on a construction site
- while moving, the robot detects an unexpected obstacle in its way (e.g. some construction materials that are stored there temporarily)
- depending on the situation, either of the following may now happen:
  - the robot may be able to autonomously circumnavigate the obstacle and continue its work
  - the robot may be blocked and thus unable to continue its work, so that it requires human support

The SWS management should support the decision if/when a human should be called to help the robot. This decision depends on:

- the time that the robot is already blocked
- the battery status of the robot
- the expected waiting time until the human will come to help

#### Example Rules

- if the battery status of the robot is low, ask for human help
- if the time the robot is already blocked is short and the battery status of the robot is not low, let the robot continue trying
- if the time the robot is already blocked is moderate and the battery status of the robot is not low, inform the human about the problem (so that they may decide if/when to help)

To explore the example rules in detail, please refer to the FCL file located [here](src/main/resources/rules/ConstructionRobotAssistanceDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the construction scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=construction
```

##### Example Request

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the `/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "timeRobotIsBlocked": 4,
      "robotBatteryStatus": 65,
      "waitingTimeForHuman": 10
    }
  }'
```

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify the values for the `slidingDecisionInputParameters` as follows:
- `timeRobotIsBlocked`: The time that the robot is blocked, measured in minutes (0-15 minutes)
- `robotBatteryStatus`: The battery status of the robot, measured in percent (0%-100%)
- `waitingTimeForHuman`: The waiting time for the human to become available, measured in minutes (0-15 minutes)

##### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "decisionResult": {
    "slidingDecision": "AI_AUTONOMOUSLY",
    "description": "Let the robot continue trying"
  }
}
```

Depending on the input, the SWS may decide one of the following:
- `AI_AUTONOMOUSLY`: "Let the robot continue trying"
- `HUMAN_ON_THE_LOOP`: "Inform human about the problem"
- `HUMAN_IN_THE_LOOP`: "Warn human, but let them decide if/when to help"
- `HUMAN_MANUALLY`: "Ask human for help"

---

## How to Read the `decisionExplanation`

To make clear how the internal rule engine reached the "sliding decision", the response JSON contains the
section `decisionExplanation`, subdivided into `inputVariables`, `appliedRules` and `outputVariables`. Each of those are
described in the following based on examples.

1. Input Variables (`inputVariables`)

```json
{
  "decisionExplanation": {
    "inputVariables": {
      "noOfTrucksInQueue": {
        "value": 7.0,
        "membershipValues": {
          "MEDIUM": 0.5,
          "LOW": 0.5
        }
      },
      "...": "..."
    }
  }
}
```

- `value`: this is the original number provided as input. For example,`"noOfTrucksInQueue": { "value": 7.0, ... }`
  indicates that the input value is `7.0`.
- `membershipValues`: each fuzzy set (e.g. `LOW` or `MEDIUM`) receives a degree of membership between 0 and 1. For
  instance, a value of `7.0` might partially belong to both the `LOW` and `MEDIUM` sets with a membership of `0.5` each.
  This shows how well the input fits each fuzzy category.

2. Applied Rules (`appliedRules`)  
   This field lists the rules that were activated during the decision-making process.

```json
{
  "appliedRules": [
    {
      "name": "1",
      "ifClause": "noOfTrucksInQueue IS LOW",
      "thenClause": "[suggestedWorkSharingApproach IS AI_AUTONOMOUSLY]",
      "weight": "1.0",
      "degreeOfSupport": "0.5"
    }
  ],
  "...": "..."
}
```

- `name`: a identifier (e.g., `"name": "1"`)
- `ifClause`: the condition of the rule (e.g., `"noOfTrucksInQueue IS LOW"`)
- `thenClause`: the outcome that the rule suggests (e.g., `"[suggestedWorkSharingApproach IS AI_AUTONOMOUSLY]"`)
- `weight` and `degreeOfSupport`: these values indicate the strength with which the rule fired based on the input’s
  membership in the corresponding fuzzy set

The activated rules are derived from, how the input membership values match the conditions of each fuzzy rule. The degree
of support shows the degree to which each rule contributed to the final decision.

3. Output Variables (`outputVariables`)  
   Shows the final outcome after evaluating the all the activated rules and the result.

```json
{
  "outputVariables": {
    "suggestedWorkSharingApproach": {
      "value": 2.998000000000003,
      "membershipValues": {
        "HUMAN_ON_THE_LOOP": 1.0
      }
    }
  }
}
```

- `value`: after combining all contributions from the fired rules, the fuzzy inference process computes a numerical value. For example, a value approximately `2.998` is produced.
- `membershipValues`: this final output is then associated with a fuzzy linguistic term. In our example, `2.998`
  maps to "HUMAN_ON_THE_LOOP" with a membership degree of `1.0`. This means that, after all rules are applied, the
  final decision is identified as that term.
