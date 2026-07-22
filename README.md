# Sliding Work Sharing

Sliding Work Sharing (SWS) Management Component of the AI4Work Project.

The aim of this software is to support dynamic work sharing between human and machine (especially AI/robots). It can
provide decision support regarding the appropriate degree of machine autonomy and the required degree of human
involvement, depending on the respective work situation.

---

## How to Build and Run the Application

### 1. Prerequisites

To build and run the Sliding Work Sharing, the following software is required:

- **Java**: Version 25 or later.
- **Apache Maven**: Version 3.9.x

### 2. Install Dependencies and Build the Application

Open a terminal in the project directory and execute the following command to clean previous build artifacts, install
dependencies and build the application:

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

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the
`/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "numberOfTrucksInQueue": 7,
      "positionOfTruckToBePrioritized": 5,
      "materialUrgency":30,
      "operationalWorkload":80
    }
  }'
```

### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "slidingDecisionOutputParameters": {
    "suggestedApproach": {
      "slidingDecision": "informHuman",
      "description": "Human has to be informed about AI's rescheduling"
    }
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```

_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is
described [here](#how-to-read-the-decisionexplanation).

---

## How to apply the SWS to your own application scenario

To apply SWS to your own application scenario, you need to do the following:

- define your own input parameters, output parameter(s) and decision rules in an `.fcl` file
- create your custom `.yml` configuration file
- download a runnable version of the software (or build it yourself)

### Create your custom `.fcl` file

- `fcl` (fuzzy control language) is used to define input parameters, output parameter and decision rules.
- our suggestion would be to take one of the existing `.fcl` files as template and adjust it to your scenario
- existing example `.fcl` files can be found at [src/main/resources/rules](src/main/resources/rules)

_Note_: The SWS application can return multiple output parameters. In your custom `.fcl` file, you
can define several decision outputs, and each one will appear as a separate parameter in the response JSON. The agriculture
scenario ([Agriculture Scenario](#agriculture-scenario)) includes an example for this feature.

### Create your custom `.yml` configuration file

- our suggestion would be to take an existing `application-{existing-configuration}.yml` as template and adjust it:
    - the `fclRulesFilePath` should point to the location of your `.fcl` file
    - the textual description of the decision results should fit to your scenario
    - replace `{existing-configuration}` with a name representing your custom scenario
- existing example configuration files can be found at [src/main/resources](src/main/resources)

### Download (or build) the sliding-work-sharing `.jar` file

- the easiest way is to download the release `.jar` file from the following link:
  https://github.com/AI4WORK-Project/sliding-work-sharing/releases/download/v0.1.1/sliding-work-sharing-0.1.1.jar
- alternatively, in case you prefer to build your own jar file, follow
  the [instructions above](#how-to-build-and-run-the-application)

### Run the application using your custom configuration

- place the following files in a single directory
    - your custom `.fcl` file
    - your custom `.yml` file
    - the `.jar` file (e.g.,`sliding-work-sharing-0.1.1.jar`)

_Note_: Ensure that the path to your `.fcl` file is correctly specified as `fclRulesFilePath` in the `.yml` file

- next, open a terminal in the same directory (where all files are located) and run the following command

```bash
java -jar sliding-work-sharing-0.1.1.jar --spring.config.location=application-{your-configuration-name}.yml
```

_Please Note_: here the `{your-configuration-name}` would be the name of your custom scenario's name

To test your custom scenario, follow the example in the [testing the application](#how-to-test-the-application)
section and adjust its input parameters to fit to your own scenario.

---

## Demonstration Scenarios

To make this software useful for application in different domains, it can be configured via
application-scenario-specific rules. This is explained in the following, based on simplified example scenarios from the
AI4Work project's pilot domains:

1. [Logistics Scenario](#logistics-scenario)
2. [Agriculture Scenario](#agriculture-scenario)
3. [Construction Scenario](#construction-scenario)

---

### Logistics Scenario

This example scenario is from the logistics domain:

- imagine a queue of trucks that deliver material to a warehouse
- usually the sequence of trucks is "first come, first served"
- an exception to the previous rule is that a truck should be prioritized if
    - the stock of some material is getting low
    - a truck is delivering that very same material

The SWS management decides in how far a human should be involved in the decision. This decision depends on:

- the number of trucks in the queue
- the position of the truck in the queue
- the urgency of the material that is being delivered by the truck
- the current workload of the warehouse

#### Example Rules

- if the queue is short and the truck is near its front, it can be automatically prioritized
- if the queue is long and the truck is near its end, a human needs to be involved in the decision

To explore the example rules in detail, please refer to the FCL file
located [here](src/main/resources/rules/TruckSchedulingSlidingDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the logistics scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=logistics
```

##### Example Request

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the
`/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "numberOfTrucksInQueue": 7,
      "positionOfTruckToBePrioritized": 5,
      "materialUrgency":30,
      "operationalWorkload":80
    }
  }'
```

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify
the values for the `slidingDecisionInputParameters` as follows:

- `numberOfTrucksInQueue`: Total number of trucks in the queue (0-20 trucks)
- `positionOfTruckToBePrioritized`: Position of the truck number that needs to be prioritization (0-20)
- `materialUrgency`: Urgency of the need for the material that is being delivered by the truck (0%-100%)
- `operationalWorkload`: Current workload of the warehouse (0%-100%)

##### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "slidingDecisionOutputParameters": {
    "suggestedApproach": {
      "slidingDecision": "informHuman",
      "description": "Human has to be informed about AI's rescheduling"
    }
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```

_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is
described [here](#how-to-read-the-decisionexplanation).

Depending on the input, the SWS may decide one of the following:

- `autonomousReprioritization`: "AI can reschedule without human involvement"
- `informHuman`: "Human has to be informed about AI's rescheduling"
- `requireHumanApproval`: "Human has to decide without AI support"

---

### Agriculture Scenario

This example is from the agriculture domain:

- imagine workers who are manually harvesting produce and putting it into boxes
- whenever a box is full of produce, it has to be carried to a central collection point
- carrying the box can be done either by the workers themselves or by an AI-powered transport drone
- the transport drone has limited capacity, so it cannot transport all boxes for all workers

For each required transport of a harvest box, the SWS management decides in how far a human (either worker or
supervisor) should be involved in the decision if this transport should be done by the drone or by the worker. This
decision depends on:

- the drone's battery level
- the availability of the drone
- the fatigue level of the worker
- the distance from the current location of the box to the central collection point

#### Example rules

- if drone battery level is low or fatigue level of worker is low, inform the supervisor about the situation
- if distance from the current location is low and the drone is currently not available or fatigue level of worker is
  low, let the worker carry the box

To explore the example rules in detail, please refer to the FCL file
located [here](src/main/resources/rules/AgricultureSchedulingSlidingDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the agriculture scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=agriculture
```

##### Example Request

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the
`/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "distanceToCentralCollectionPoint": 250,
      "fatigueLevelOfWorker": 80,
      "droneBatteryLevel":80,
      "isDroneCurrentlyAvailable":1
    }
}'
```

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify
the values for the `slidingDecisionInputParameters` as follows:

- `distanceToCentralCollectionPoint`: The distance to the collection point, measured in meters (0-300 meters)
- `fatigueLevelOfWorker`: The current fatigue level of the worker, measured in percent (0%-100%)
- `droneBatteryLevel`: The current battery level of the drone, measured in percent (0%-100%)
- `isDroneCurrentlyAvailable`: Whether the drone is currently available (1 = yes, 0 = no)

##### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "slidingDecisionOutputParameters": {
    "suggestedApproach": {
      "slidingDecision": "droneShouldCarryTheBox",
      "description": "Let the drone carry the box"
    },
    "shouldSupervisorBeInformed": {
      "slidingDecision": "yes",
      "description": "Inform the supervisor"
    }
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```

_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is
described [here](#how-to-read-the-decisionexplanation).

- This application scenario provides an example how the SWS can return multiple output parameters. In the `.fcl`
  file, several decision outputs can be defined, and each one will appear as a separate parameter in the response JSON.

Depending on the input parameters, the SWS may decide one of the following:

- `droneShouldCarryTheBox`: "Let the drone carry the box"
- `letTheSupervisorDecide`: "Let the supervisor decide"
- `letTheWorkerDecide`: "Let the worker decide"
- `humanShouldCarryTheBox`: "Let the worker carry the box"

---

### Construction Scenario

This example scenario is from the construction domain:

- imagine a robot doing work on a construction site
- while moving, the robot detects an unexpected obstacle in its way (e.g. some construction materials that are stored
  there temporarily)
- depending on the situation, either of the following may now happen:
    - the robot may be able to autonomously circumnavigate the obstacle and continue its work
    - the robot may be blocked and thus unable to continue its work, so that it requires human support

The SWS management should support the decision if/when a human should be called to help the robot. This decision depends
on:

- the time that the robot is already moving
- the battery status of the robot
- the number of humans currently present in the room

#### Example Rules

- if the robot battery status is low or the time the robot is already moving is long, ask for human help
- if the time robot is already moving is short and robot battery status is not low, let the robot continue trying

To explore the example rules in detail, please refer to the FCL file
located [here](src/main/resources/rules/ConstructionRobotAssistanceDecisionRules.fcl).

#### How to Run and Test the Scenario

To start the application and run the construction scenario, use the following command:

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=construction
```

##### Example Request

Execute the following `curl` command in your terminal to request a "sliding decision" via a POST request to the
`/sliding-decision` endpoint:

```bash
curl --request POST \
  --url http://localhost:8080/sliding-decision \
  --header "Content-Type: application/json" \
  --data '{
    "decisionStatus": "Sliding Decision Request",
    "slidingDecisionInputParameters": {
      "timeTheRobotIsAlreadyMoving": 4,
      "robotBatteryStatus": 65,
      "noOfHumansInTheRoom": 10
    }
  }'
```

To test the implemented rules, you can pass different inputs to the application and observe the outcomes. Just modify
the values for the `slidingDecisionInputParameters` as follows:

- `timeTheRobotIsAlreadyMoving`: The time that the robot is already moving, measured in minutes (0-15 minutes); a higher
  time may indicate that the robot is blocked
- `robotBatteryStatus`: The battery status of the robot, measured in percent (0%-100%)
- `noOfHumansInTheRoom`: The number of humans currently present in the room (0-20 humans)

##### Example Response

The application will respond with a JSON string similar to the following:

```json
{
  "decisionStatus": "Sliding Decision Response",
  "slidingDecisionOutputParameters": {
    "suggestedApproach": {
      "slidingDecision": "askForHumanHelp",
      "description": "Ask human for help"
    }
  },
  "decisionExplanation": {
    "...": "..."
  }
}
```

_Please Note_: The `decisionExplanation` is not shown here for the sake of brevity. An example is
described [here](#how-to-read-the-decisionexplanation).

Depending on the input, the SWS may decide one of the following:

- `letRobotContinue`: "Let the robot continue trying"
- `informHumanAboutSituation`: "Inform human about the situation"
- `askForHumanHelp`: "Ask human for help"

---

## How to Read the `decisionExplanation`

To make clear how the internal rule engine reached the "sliding decision", the response JSON contains the section
`decisionExplanation`, subdivided into `inputVariables`, `appliedRules` and `outputVariables`. Each of those are
described in the following based on examples.

### Input Variables

```json
{
  "decisionExplanation": {
    "inputVariables": {
      "numberOfTrucksInQueue": {
        "value": 7.0,
        "membershipValues": {
          "moderate": 1.0
        }
      },
      "...": "..."
    }
  }
}
```

- `value`: this is the original number provided as input in the Sliding Decision Request.
- `membershipValues`: this shows the "fuzzy categories" into which the input value fits. Each category is assigned a
  membership degree between 0 and 1. In the given example, the input value of `7.0` belongs to the `moderate` category
  with a membership degree of `1.0`.

### Applied Rules

This field lists the rules that were activated during the decision-making process.

```json
{
  "appliedRules": [
    {
      "name": "1",
      "condition": "IF numberOfTrucksInQueue IS moderate",
      "consequence": "THEN [suggestedWorkSharingApproach IS autonomousReprioritization]",
      "weight": "1.0",
      "degreeOfSupport": "1.0"
    }
  ],
  "...": "..."
}
```

- `name`: an identifier for the rule
- `condition`:  the part that decides if this rule should be activated, based on the input values' memberships in the
  fuzzy categories
- `consequence`: the outcome that the rule suggests (e.g.,
  `"[suggestedWorkSharingApproach IS autonomousReprioritization]"`)
- `weight`: is a pre-defined value, which defines the general "importance/impact" of this rule
- `degreeOfSupport`: the level of impact that this rule has on the final decision, calculated based on the fuzzy
  membership degrees of the input values

### Output Variables

Shows the final outcome after evaluating all the activated rules.

```json
{
  "outputVariables": {
    "suggestedWorkSharingApproach": {
      "value": 1.4977511244377752,
      "membershipValues": {
        "informHuman": 1.0
      }
    }
  }
}
```

- `value`: after combining all contributions from the fired rules, the fuzzy inference process computes a numerical
  value. In the given example, a value of approximately `1.497` is produced.
- `membershipValues`: this final output is then associated with a fuzzy category. In our example, `1.497`
  maps to "informHuman" with a membership degree of `1.0`. This means that, after all rules are applied, the final
  decision is identified as that suggested work sharing approach.
