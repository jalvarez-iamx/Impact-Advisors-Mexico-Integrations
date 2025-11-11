def two_sum(nums, target):
    """
    Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.

    Args:
        nums (List[int]): List of integers
        target (int): Target sum

    Returns:
        List[int]: Indices of the two numbers that add up to target
    """
    # Using a dictionary to store the complement and its index
    num_map = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in num_map:
            return [num_map[complement], i]
        num_map[num] = i
    # If no solution found (though problem guarantees one)
    return []

# Test cases
if __name__ == "__main__":
    print(two_sum([2, 7, 11, 15], 9))  # Output: [0, 1]
    print(two_sum([3, 2, 4], 6))        # Output: [1, 2]
    print(two_sum([3, 3], 6))           # Output: [0, 1]