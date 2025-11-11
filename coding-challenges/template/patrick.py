def two_sum(nums, target):
    """
    Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.

    Args:
        nums (List[int]): List of integers
        target (int): Target sum

    Returns:
        List[int]: Indices of the two numbers that add up to target
    """
    # Sort the array and use two pointers
    sorted_nums = sorted(enumerate(nums), key=lambda x: x[1])
    left, right = 0, len(nums) - 1

    while left < right:
        current_sum = sorted_nums[left][1] + sorted_nums[right][1]
        if current_sum == target:
            # Return original indices
            return sorted([sorted_nums[left][0], sorted_nums[right][0]])
        elif current_sum < target:
            left += 1
        else:
            right -= 1

    # If no solution found
    return []

# Test cases
if __name__ == "__main__":
    print(two_sum([2, 7, 11, 15], 9))  # Output: [0, 1]
    print(two_sum([3, 2, 4], 6))        # Output: [1, 2]
    print(two_sum([3, 3], 6))           # Output: [0, 1]