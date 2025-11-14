{
    init: function(elevators, floors) {
        // upCalls: Object to track floors where the up button has been pressed.
        // Key: floor index, Value: timestamp when the call was made (Date.now()).
        // Used to prevent duplicate calls and track pending up requests.
        var upCalls = {};

        // downCalls: Object to track floors where the down button has been pressed.
        // Key: floor index, Value: timestamp when the call was made (Date.now()).
        // Used to prevent duplicate calls and track pending down requests.
        var downCalls = {};

        // callTimes: Object to track the timestamp of all calls (up and down).
        // Key: 'floorIndex_direction' (e.g., '5_up'), Value: timestamp (Date.now()).
        // Used to calculate wait times for calls.
        var callTimes = {};

        // lastJobTime: Object to track when each elevator last received a job.
        // Key: elevator index, Value: timestamp (Date.now()).
        // Helps in prioritizing elevators that haven't worked recently.
        var lastJobTime = {}; // Track when each elevator last got a job

        // elevatorIdleStart: Object to track when each elevator became idle.
        // Key: elevator index, Value: timestamp (Date.now()) when it became idle.
        // Used to calculate idle time for prioritization.
        var elevatorIdleStart = {}; // Track when elevator became idle

        // Initialize job times for each elevator
        elevators.forEach(function(e, i) {
            lastJobTime[i] = 0;
            elevatorIdleStart[i] = 0;
        });

        // Set up event listeners for floor buttons
        floors.forEach(function(floor, i) {
            floor.on("up_button_pressed", function() {
                // Only record if not already called
                if (!upCalls[i]) {
                    upCalls[i] = Date.now();
                    callTimes[i + '_up'] = Date.now();
                }
            });
            floor.on("down_button_pressed", function() {
                // Only record if not already called
                if (!downCalls[i]) {
                    downCalls[i] = Date.now();
                    callTimes[i + '_down'] = Date.now();
                }
            });
        });

        // getWaitTime: Function to calculate how long a call has been waiting.
        // Parameters: floor (number), direction ('up' or 'down').
        // Returns: wait time in seconds (number).
        // If no call exists, returns 0.
        function getWaitTime(floor, direction) {
            var key = floor + '_' + direction;
            if (!callTimes[key]) return 0;
            return (Date.now() - callTimes[key]) / 1000;
        }

        // getIdleTime: Function to calculate how long an elevator has been idle.
        // Parameters: elevatorId (number, index of elevator).
        // Returns: idle time in milliseconds (number).
        // If never idle, returns 0.
        function getIdleTime(elevatorId) {
            if (!elevatorIdleStart[elevatorId]) return 0;
            return Date.now() - elevatorIdleStart[elevatorId];
        }

        // findBestElevator: Function to select the best elevator for a call.
        // Parameters: targetFloor (number), direction ('up' or 'down'), urgency (number, 0-1).
        // Returns: best elevator object or null if none available.
        // Considers distance, direction, idle status, load, etc.
        function findBestElevator(targetFloor, direction, urgency) {
            // candidates: Array to hold potential elevators with their metrics.
            var candidates = [];

            elevators.forEach(function(e, idx) {
                // cf: current floor of the elevator.
                var cf = e.currentFloor();
                // dir: current direction of the elevator ('up', 'down', 'stopped').
                var dir = e.destinationDirection();
                // queue: array of destination floors for the elevator.
                var queue = e.destinationQueue;
                // load: load factor of the elevator (0-1, where 1 is full).
                var load = e.loadFactor();

                // Skip elevators that are too full (>85% capacity)
                if (load > 0.85) return;

                // dist: absolute distance from current floor to target floor.
                var dist = Math.abs(cf - targetFloor);

                // sameDirection: boolean indicating if elevator is going same direction and can pick up.
                var sameDirection = false;
                if (dir === direction) {
                    if (direction === "up" && cf <= targetFloor) {
                        sameDirection = true;
                    } else if (direction === "down" && cf >= targetFloor) {
                        sameDirection = true;
                    }
                }

                // isIdle: boolean indicating if elevator has no destinations and is stopped.
                var isIdle = queue.length === 0 && (dir === "stopped" || !dir);

                // idleTime: how long the elevator has been idle (milliseconds).
                var idleTime = getIdleTime(idx);

                // Add elevator to candidates with all relevant metrics
                candidates.push({
                    elevator: e,
                    index: idx,
                    distance: dist,
                    sameDirection: sameDirection,
                    isIdle: isIdle,
                    idleTime: idleTime,
                    queueLength: queue.length,
                    load: load
                });
            });

            if (candidates.length === 0) return null;

            // Sort candidates by priority hierarchy:
            // 1. Same direction elevators (closest first)
            // 2. Idle elevators (closest first, then longest idle)
            // 3. Other elevators (closest first, then longest idle)
            candidates.sort(function(a, b) {
                // For urgent calls (>80% urgency), prioritize same direction, then distance, then idle time
                if (urgency > 0.8) {
                    if (a.sameDirection && !b.sameDirection) return -1;
                    if (!a.sameDirection && b.sameDirection) return 1;
                    if (a.distance !== b.distance) return a.distance - b.distance;
                    return b.idleTime - a.idleTime;
                }

                // Priority 1: Same direction elevators
                if (a.sameDirection && !b.sameDirection) return -1;
                if (!a.sameDirection && b.sameDirection) return 1;

                if (a.sameDirection && b.sameDirection) {
                    // Both same direction - closest wins
                    if (a.distance !== b.distance) return a.distance - b.distance;
                    // If equidistant, longest idle wins
                    return b.idleTime - a.idleTime;
                }

                // Priority 2: Idle elevators
                if (a.isIdle && !b.isIdle) return -1;
                if (!a.isIdle && b.isIdle) return 1;

                if (a.isIdle && b.isIdle) {
                    // Both idle - closest wins
                    if (a.distance !== b.distance) return a.distance - b.distance;
                    // If equidistant, longest idle wins (or lower index at start)
                    if (a.idleTime === 0 && b.idleTime === 0) return a.index - b.index;
                    return b.idleTime - a.idleTime;
                }

                // Priority 3: Busy elevators - closest and least busy
                if (a.distance !== b.distance) return a.distance - b.distance;
                if (a.queueLength !== b.queueLength) return a.queueLength - b.queueLength;
                return b.idleTime - a.idleTime;
            });

            return candidates[0].elevator;
        }

        // insertFloor: Function to add a floor to an elevator's destination queue optimally.
        // Parameters: elevator (object), floor (number), priority (boolean).
        // Inserts the floor in the best position based on current direction and priority.
        function insertFloor(elevator, floor, priority) {
            var queue = elevator.destinationQueue;
            // Don't add if already in queue
            if (queue.indexOf(floor) !== -1) return;

            var cf = elevator.currentFloor();
            var dir = elevator.destinationDirection();

            if (queue.length === 0) {
                queue.push(floor);
            } else if (priority) {
                // High priority - insert intelligently
                if ((dir === "up" && floor > cf) || (dir === "down" && floor < cf)) {
                    queue.unshift(floor); // Add to front
                } else {
                    queue.push(floor); // Add to end
                }
            } else {
                // Insert in optimal position based on direction
                if ((dir === "up" || (dir === "stopped" && floor > cf)) && floor >= cf) {
                    var inserted = false;
                    for (var i = 0; i < queue.length; i++) {
                        if (queue[i] > floor) {
                            queue.splice(i, 0, floor);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) queue.push(floor);
                } else if ((dir === "down" || (dir === "stopped" && floor < cf)) && floor <= cf) {
                    var inserted = false;
                    for (var i = 0; i < queue.length; i++) {
                        if (queue[i] < floor) {
                            queue.splice(i, 0, floor);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) queue.push(floor);
                } else {
                    queue.push(floor);
                }
            }

            elevator.destinationQueue = queue;
            elevator.checkDestinationQueue();
        }

        // Set up event listeners for each elevator
        elevators.forEach(function(elevator, elevatorId) {

            elevator.on("idle", function() {
                // Mark as idle - DO NOT MOVE
                elevatorIdleStart[elevatorId] = Date.now();
                // Clear destination queue to prevent unnecessary moves
                elevator.destinationQueue = [];
                elevator.checkDestinationQueue();
            });

            elevator.on("floor_button_pressed", function(floorNum) {
                // Add floor to queue when passenger presses button inside elevator
                insertFloor(elevator, floorNum, false);
            });

            elevator.on("passing_floor", function(floorNum, direction) {
                // Check if we should stop at this floor for waiting passengers
                if (elevator.loadFactor() < 0.65) { // Only if not too full
                    var waitTime = 0;
                    var hasCall = false;

                    if (direction === "up" && upCalls[floorNum]) {
                        waitTime = getWaitTime(floorNum, 'up');
                        hasCall = true;
                    } else if (direction === "down" && downCalls[floorNum]) {
                        waitTime = getWaitTime(floorNum, 'down');
                        hasCall = true;
                    }

                    // Only stop if urgent (>9s wait) or very light load (<40%)
                    if (hasCall && (waitTime > 9 || elevator.loadFactor() < 0.4)) {
                        insertFloor(elevator, floorNum, waitTime > 9); // High priority if very urgent
                    }
                }
            });

            elevator.on("stopped_at_floor", function(floorNum) {
                // Clear the call for this floor since we've arrived
                delete upCalls[floorNum];
                delete downCalls[floorNum];
                delete callTimes[floorNum + '_up'];
                delete callTimes[floorNum + '_down'];

                // If queue is now empty, mark as idle
                if (elevator.destinationQueue.length === 0) {
                    elevatorIdleStart[elevatorId] = Date.now();
                }
            });
        });

        // Job assignment loop - runs every 100ms to assign unhandled calls
        setInterval(function() {
            // allCalls: Array to collect all pending calls with their details.
            var allCalls = [];

            // Collect all pending up calls
            for (var f in upCalls) {
                var floor = parseInt(f);
                var waitTime = getWaitTime(floor, 'up');
                allCalls.push({
                    floor: floor,
                    direction: 'up',
                    wait: waitTime,
                    urgency: waitTime / 10// Urgency scales with wait time (10 = 1.0)
                });
            }
            // Collect all pending down calls
            for (var f in downCalls) {
                var floor = parseInt(f);
                var waitTime = getWaitTime(floor, 'down');
                allCalls.push({
                    floor: floor,
                    direction: 'down',
                    wait: waitTime,
                    urgency: waitTime / 10
                });
            }

            // Sort calls by urgency (most urgent first)
            allCalls.sort(function(a, b) { return b.urgency - a.urgency; });

            // Assign each unhandled call to the best available elevator
            allCalls.forEach(function(call) {
                // Check if already assigned (in some elevator's queue)
                var isHandled = elevators.some(function(e) {
                    return e.destinationQueue.indexOf(call.floor) !== -1;
                });

                if (!isHandled) {
                    var e = findBestElevator(call.floor, call.direction, call.urgency);
                    if (e) {
                        var elevatorIdx = elevators.indexOf(e);
                        insertFloor(e, call.floor, call.urgency > 0.7); // High priority if urgent
                        lastJobTime[elevatorIdx] = Date.now();
                        elevatorIdleStart[elevatorIdx] = 0; // No longer idle
                    }
                }
            });

        }, 100); // Check every 100ms
    },

    update: function(dt, elevators, floors) {
        // Game loop - called every frame, but not used in this implementation
    }
}