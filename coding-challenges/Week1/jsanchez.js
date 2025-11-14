{
    init: function(elevators, floors) {
        // Use the approach that got us 93/100
        var nextElevator = 0;

        elevators.forEach(function(elevator, index) {
            elevator.goToFloor(index);

            elevator.on("idle", function() {
                // Do nothing - wait for work
            });

            elevator.on("floor_button_pressed", function(floorNum) {
                elevator.goToFloor(floorNum);
            });
        });

        floors.forEach(function(floor) {
            floor.on("up_button_pressed", function() {
                elevators[nextElevator].goToFloor(floor.floorNum());
                nextElevator = (nextElevator + 1) % elevators.length;
            });

            floor.on("down_button_pressed", function() {
                elevators[nextElevator].goToFloor(floor.floorNum());
                nextElevator = (nextElevator + 1) % elevators.length;
            });
        });
    },
        update: function(dt, elevators, floors) {
            // SIMPLE AND EFFECTIVE: Just find work for truly idle elevators
            elevators.forEach(function(elevator) {
                if (elevator.destinationQueue.length === 0) {
                    for (var i = 0; i < 6; i++) {
                        if (floors[i].buttonStates.up === 'activated' || 
                            floors[i].buttonStates.down === 'activated') {
                            elevator.goToFloor(i);
                            break;
                        }
                    }
                }
            });
        }
}