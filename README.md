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
    "inputParameters": {
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
  "decisionResults": {
    "slidingDecision": "AI_AUTONOMOUSLY",
    "description": "AI can reschedule without human involvement"
  }
}
```

--- 

## Demonstration scenario

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

Note: To test the implemented rules, you can pass different input parameters to the application and observe the outcomes. Just modify the values for the `inputParameters` in the above mentioned [Example Request](#example-request). 

