/*
 * Optimized Elevator Control Script for Elevator Saga
 * Author: Jose Antonio Alvarez Moreno
 *
 * This script implements an efficient elevator management system that minimizes waiting times
 * and maximizes passenger throughput by intelligently assigning elevators to requests and
 * allowing opportunistic pickups during travel.
 */

{
    init: function(elevators, floors) {
        /*
         * The init function is called once at the start of the simulation.
         * It sets up event handlers and initializes data structures.
         * Essential: This is the entry point for configuring elevator behavior.
         * Optimization potential: Could be extended to support zoning (assigning elevators to floor ranges).
         */

        /*
         * upRequests: Array to store floor numbers requesting upward travel.
         * Why chosen: Simple array provides FIFO (First-In-First-Out) ordering, which is fair and easy to implement.
         * Logic: When a floor presses the up button, we add the floor number here if not already present.
         * This ensures we don't process duplicate requests and can service them in the order received.
         * Essential: Tracks pending up calls; without this, elevators wouldn't know where to go.
         * Optimization: Could be a priority queue sorted by floor number or wait time for more efficient servicing.
         */
        var upRequests = [];

        /*
         * downRequests: Array to store floor numbers requesting downward travel.
         * Why chosen: Mirrors upRequests for symmetry and separation of concerns.
         * Logic: Similar to upRequests but for down direction; prevents mixing up/down logic.
         * Essential: Allows directional awareness, crucial for efficient elevator operation.
         * Optimization: Could be combined into a single queue with direction metadata, but separation simplifies logic.
         */
        var downRequests = [];

        /*
         * Function to assign the closest available elevator to a floor request.
         * This reduces unnecessary travel and improves response time.
         * Essential: Ensures requests are handled by the most suitable elevator.
         * Optimization: Could incorporate load balancing by checking elevator capacity (loadFactor).
         */
        /*
         * assignElevator function: Assigns the most suitable elevator to a floor request.
         * Parameters:
         *   - floorNum: The floor number making the request (integer).
         *   - direction: The direction of travel requested ("up" or "down").
         * Why chosen: Function encapsulates assignment logic for reusability and clarity.
         * Logic: Finds the elevator with the smallest distance to the requesting floor,
         * preferring idle elevators or those already moving in the same direction.
         * Essential: Ensures efficient assignment; without this, elevators might be assigned randomly or inefficiently.
         * Optimization: Could factor in elevator speed, current load, or predicted future requests.
         */
        function assignElevator(floorNum, direction) {
            /*
             * bestElevator: Variable to track the most suitable elevator found so far.
             * Why chosen: Null initialization allows easy checking if any elevator was found.
             * Logic: Updated whenever a closer elevator is discovered.
             * Essential: Holds the final assignment target.
             */
            var bestElevator = null;

            /*
             * minDistance: Tracks the smallest distance found to a requesting floor.
             * Why chosen: Infinity as initial value ensures any real distance will be smaller.
             * Logic: Used to compare and select the closest elevator.
             * Essential: Criterion for choosing the "best" elevator.
             * Optimization: Could use Manhattan distance or time-based metrics instead of absolute distance.
             */
            var minDistance = Infinity;

            /*
             * Loop through all elevators using forEach to evaluate each one.
             * Why chosen: forEach is simple and readable for array iteration in JavaScript.
             * Logic: Checks each elevator's suitability for the request.
             * Essential: Ensures we consider every elevator for optimal assignment.
             * Optimization: Could use a more efficient search algorithm if there are many elevators.
             */
            elevators.forEach(function(elevator) {
                /*
                 * Condition: elevator.destinationQueue.length === 0 || elevator.destinationDirection() === direction
                 * Why chosen: Checks if elevator is idle (no destinations) or already going the same way.
                 * Logic: Idle elevators can be assigned immediately; same-direction elevators can pick up en route.
                 * Essential: Prevents assigning elevators that would need to change direction unnecessarily.
                 * Optimization: Could also check loadFactor() to avoid overloading elevators.
                 */
                if (elevator.destinationQueue.length === 0 || elevator.destinationDirection() === direction) {
                    /*
                     * distance: Absolute difference between elevator's current floor and requested floor.
                     * Why chosen: Simple Euclidean distance on a 1D floor line.
                     * Logic: Smaller distance means faster response time.
                     * Essential: Primary criterion for selecting the closest elevator.
                     * Optimization: Could factor in elevator speed or door opening time for more accurate estimates.
                     */
                    var distance = Math.abs(elevator.currentFloor() - floorNum);

                    /*
                     * Comparison: if (distance < minDistance)
                     * Logic: Updates bestElevator if this one is closer than previously found elevators.
                     * Essential: Ensures we always keep track of the closest suitable elevator.
                     */
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestElevator = elevator;
                    }
                }
            });

            /*
             * Final assignment: if (bestElevator)
             * Logic: Only assign if a suitable elevator was found.
             * Essential: Sends the elevator to the requested floor, fulfilling the user's call.
             * Optimization: Could queue the request if no elevator is available, but in this implementation we assume at least one elevator is always assignable.
             */
            if (bestElevator) {
                bestElevator.goToFloor(floorNum);
            }
        }

        /*
         * Set up event handlers for floor button presses.
         * Essential: Responds to user inputs from floors.
         * Optimization: Could debounce rapid presses or prioritize based on crowd size (if available).
         */
        /*
         * Set up event handlers for each floor's buttons.
         * Why chosen: forEach loop ensures every floor is handled consistently.
         * Logic: Attaches listeners to up and down buttons on each floor.
         * Essential: Allows the system to respond to user inputs from any floor.
         * Optimization: Could be optimized for buildings with many floors by using event delegation.
         */
        floors.forEach(function(floor) {
            /*
             * Event handler for up_button_pressed.
             * Logic: When up button is pressed, check if floor is already in upRequests.
             * If not, add it and immediately try to assign an elevator.
             * Essential: Registers upward travel requests and triggers assignment.
             * Optimization: Could debounce multiple presses or prioritize based on wait time.
             */
            floor.on("up_button_pressed", function() {
                if (upRequests.indexOf(floor.floorNum()) === -1) {
                    upRequests.push(floor.floorNum());
                    assignElevator(floor.floorNum(), "up");
                }
            });

            /*
             * Event handler for down_button_pressed.
             * Logic: Similar to up button but for downward requests.
             * Essential: Registers downward travel requests symmetrically.
             * Optimization: Same as up button handler.
             */
            floor.on("down_button_pressed", function() {
                if (downRequests.indexOf(floor.floorNum()) === -1) {
                    downRequests.push(floor.floorNum());
                    assignElevator(floor.floorNum(), "down");
                }
            });
        });

        /*
         * Set up event handlers for each elevator.
         * Essential: Defines elevator behavior in response to various events.
         * Optimization: Could implement more sophisticated state machines for complex scenarios.
         */
        /*
         * Set up event handlers for each elevator.
         * Why chosen: forEach ensures consistent behavior across all elevators.
         * Logic: Defines how each elevator responds to various events.
         * Essential: Core of the elevator logic; without this, elevators wouldn't respond to events.
         * Optimization: Could use a more sophisticated state machine for complex behaviors.
         */
        elevators.forEach(function(elevator) {
            /*
             * Event handler for "idle" event.
             * Logic: When elevator becomes idle, check for pending requests and service the first one.
             * Prioritizes up requests over down requests (arbitrary but consistent choice).
             * Essential: Prevents elevators from sitting idle when there are calls to answer.
             * Optimization: Could implement more sophisticated prioritization based on wait times or floor proximity.
             */
            elevator.on("idle", function() {
                if (upRequests.length > 0) {
                    /*
                     * nextFloor: The floor number retrieved from the front of upRequests queue.
                     * Why chosen: shift() removes and returns the first element (FIFO).
                     * Logic: Services the oldest up request first.
                     */
                    var nextFloor = upRequests.shift();
                    elevator.goToFloor(nextFloor);
                } else if (downRequests.length > 0) {
                    /*
                     * nextFloor: Similar to above but for down requests.
                     */
                    var nextFloor = downRequests.shift();
                    elevator.goToFloor(nextFloor);
                }
            });

            /*
             * Event handler for "floor_button_pressed" (inside elevator).
             * Logic: When a passenger presses a floor button inside, add it to the elevator's destination queue.
             * Essential: Allows passengers to specify their destination.
             * Optimization: Could check if floor is already in queue or validate the request.
             */
            elevator.on("floor_button_pressed", function(floorNum) {
                elevator.goToFloor(floorNum);
            });

            /*
             * Event handler for "passing_floor".
             * Logic: If elevator is moving and passes a floor with a pending request in the same direction,
             * pick up the request by adding the floor to the destination queue.
             * Essential: Enables opportunistic servicing, reducing overall wait times.
             * Optimization: Could check elevator capacity before picking up additional passengers.
             */
            elevator.on("passing_floor", function(floorNum, direction) {
                if (direction === "up" && upRequests.indexOf(floorNum) !== -1) {
                    /*
                     * Remove floorNum from upRequests using splice.
                     * Why chosen: splice(index, 1) removes exactly one element at the specified index.
                     * Logic: Ensures we don't service the same request multiple times.
                     */
                    upRequests.splice(upRequests.indexOf(floorNum), 1);
                    elevator.goToFloor(floorNum);
                } else if (direction === "down" && downRequests.indexOf(floorNum) !== -1) {
                    /*
                     * Similar logic for down requests.
                     */
                    downRequests.splice(downRequests.indexOf(floorNum), 1);
                    elevator.goToFloor(floorNum);
                }
            });

            /*
             * Event handler for "stopped_at_floor".
             * Logic: When elevator stops at a floor, remove any pending requests for that floor
             * from both queues (in case there were both up and down requests).
             * Essential: Cleans up fulfilled requests to prevent duplicate servicing.
             * Optimization: Could track which direction was serviced for more precise cleanup.
             */
            elevator.on("stopped_at_floor", function(floorNum) {
                /*
                 * upIndex: Index of floorNum in upRequests array, or -1 if not found.
                 * Why chosen: indexOf returns the index for easy removal.
                 * Logic: Find and remove the floor from up requests if present.
                 */
                var upIndex = upRequests.indexOf(floorNum);
                if (upIndex !== -1) {
                    upRequests.splice(upIndex, 1);
                }

                /*
                 * downIndex: Similar to upIndex but for downRequests.
                 */
                var downIndex = downRequests.indexOf(floorNum);
                if (downIndex !== -1) {
                    downRequests.splice(downIndex, 1);
                }
            });
        });
    },

        update: function(dt, elevators, floors) {
            // We normally don't need to do anything here
        }
}