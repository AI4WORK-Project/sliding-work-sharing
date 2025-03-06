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
  "decisionExplanation": {}
}
```
_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is described [here](#explanation-of-the-decisionexplanation-field).  

---

## Demonstration Scenarios

To make this software useful for application in different domains, it can be configured via application-scenario-specific rules. This is explained in the following, based on simplified example scenarios from the AI4Work project's pilot domains:

1. [Logistics Scenario](#logistics-scenario)
2. [Agriculture Scenario](#agriculture-scenario)

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
  "decisionExplanation": {}
}
```

_Note: The actual `decisionExplanation` field will include details about decision explanation. Please refer to the explanation of the `decisionExplanation` here in the section [Explanation of the `decisionExplanation` Field](#explanation-of-the-decisionexplanation-field)._

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
- if the waiting time for the drone is high and the distance is high and the fatigue level of the worker is low, then let the worker decide (if or if not to wait for the drone)
- if the waiting time for the drone is high and the fatigue level of the worker is high, let the drone carry the box, but inform the supervisor (who may intervene and decide to not wait for the drone)

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
- `fatigueLevelOfWorker`: The current fatigue level of the worker, measured in percentage (0%-100%)

##### Example Response
The application will respond with a JSON string similar to the following:

```json
{
    "decisionStatus": "Sliding Decision Response",
    "decisionResult": {
        "slidingDecision": "AI_AUTONOMOUSLY",
        "description": "Let the drone carry the box"
    },
  "decisionExplanation": {}
}
```
_Note: The actual `decisionExplanation` field will include details about decision explanation. Please refer to the explanation of the `decisionExplanation` here in the section [Explanation of the `decisionExplanation` Field](#explanation-of-the-decisionexplanation-field)._

Depending on the input parameters, the SWS may decide one of the following:
- `AI_AUTONOMOUSLY`: "Let the drone carry the box"
- `HUMAN_ON_THE_LOOP`: "Inform supervisor, who may potentially intervene"
- `HUMAN_IN_THE_LOOP`: "Let the worker decide"
- `HUMAN_MANUALLY`: "Let the worker carry the box"

---

## How to Read the `decisionExplanation` 
The Example of `decisionExplanation` field:

```json
{
  "decisionExplanation": {
    "inputVariables": {
      "noOfTrucksInQueue": {
        "value": 7.0,
        "terms": {
          "MEDIUM": 0.5,
          "LOW": 0.5
        }
      },
      "positionOfTruckToBePrioritized": {
        "value": 5.0,
        "terms": {
          "NEAR_THE_FRONT_OF_THE_QUEUE": 1.0
        }
      }
    },
    "appliedRules": [
      {
        "rule": "1 (0.5) if noOfTrucksInQueue IS LOW then suggestedWorkSharingApproach IS AI_AUTONOMOUSLY [weight: 1.0]"
      },
      {
        "rule": "2 (0.5) if (noOfTrucksInQueue IS MEDIUM) AND (positionOfTruckToBePrioritized IS NEAR_THE_FRONT_OF_THE_QUEUE) then suggestedWorkSharingApproach IS HUMAN_ON_THE_LOOP [weight: 1.0]"
      }
    ],
    "outputVariables": {
      "suggestedWorkSharingApproach": {
        "value": 2.998,
        "terms": {
          "HUMAN_ON_THE_LOOP": 1.0
        }
      }
    }
  }
}
```

To make clear how the internal rule engine reached the "sliding decision", the response JSON contains the section `decisionExplanation`, subdivided into `inputVariables`, `appliedRules` and `outputVariables`. Each of those are described in the following based on examples.

- `inputVariables`:
  - `value`: the "slidingDecisionInputParameters", provided as an input.
    - Example: `noOfTrucksInQueue`, the value could be `7.0`.
  - `term`: these are the fuzzy sets (or linguistic terms) defined in the FUZZIFY sections in the `.fcl` file. Each term has an associated membership in between `0` and `1` that measures how input values belongs to that fuzzy set. It translates input values into fuzzy concepts (e.g., LOW, MEDIUM, HIGH) based on pre-defined membership functions.
    - For instance, the membership function (e.g., for `LOW` defined as `(0,1) (5,1) (9,0)`) is evaluated with the input value to a degree. For an input of `7.0`, this might result in a partial membership of `0.5` for both `LOW` and `MEDIUM`.
- `appliedRules`:  
  Lists the fuzzy rules that were activated during the decision-making process. Defined in the `RULEBLOCK` in the `.fcl` file. The rule’s condition is satisfied, it is derived from the membership degrees of the inputs.
  - For instance, if `noOfTrucksInQueue` has a membership of `0.5` in `LOW`, then "Rule 1" might fire with a strength of `0.5`.
- `outputVariables`:  
  Shows the final outcome after evaluating the all the activated rules and defuzzifying the result.
  - `value`: the output value computed from the fuzzy inference process.
    - In example, the output value is approximately `2.998`.
  - `terms`: represents the membership degree of the output value to each fuzzy set defined in the DEFUZZIFY section in the `.fcl` file. The defuzzification process takes all the contributions from activated rules and produces a single value (`value`). This number is then mapped to the fuzzy linguistic terms.
    - For instance, if `2.998` corresponds to `HUMAN_ON_THE_LOOP`, it might be represented as having a membership degree of `1.0` for that term.
