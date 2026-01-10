---
layout: dsa_post
title: "Remove Duplicates from Sorted Array"
category: Array
order: 1
excerpt: "Remove duplicates from a sorted array in-place such that each unique element appears only once."
---

# Problem

Given an integer array `nums` sorted in **non-decreasing order**, remove the duplicates **in-place** such that each unique element appears only once. The relative order of the elements should be kept the same.

Consider the number of unique elements in `nums` to be `k`. After removing duplicates, return the number of unique elements `k`.

The first `k` elements of `nums` should contain the unique numbers in sorted order. The remaining elements beyond index `k - 1` can be ignored.

### Example 1:

```
Input: nums = [1,1,2]
Output: 2, nums = [1,2,_]
Explanation: Your function should return k = 2, with the first two elements of nums being 1 and 2 respectively.
```

### Example 2:

```
Input: nums = [0,0,1,1,1,2,2,3,3,4]
Output: 5, nums = [0,1,2,3,4,_,_,_,_,_]
Explanation: Your function should return k = 5, with the first five elements of nums being 0, 1, 2, 3, and 4 respectively.
```

---

# Approach & Explanation

Since the array is **sorted**, all duplicate elements will be next to each other. We can use the **Two-Pointer Technique** to solving this efficiently in linear time `O(n)` and constant extra space `O(1)`.

![Remove Duplicates Visualization]({{ "/assets/images/dsa-remove-duplicates.png" | relative_url }})

### The Two Pointers
1.  **`k` (The "Writer")**: Points to the index where the *next unique element* should be written. It basically tracks the length of the unique prefix.
2.  **`i` (The "Reader")**: Scans through the array from left to right.

### Algorithm
1.  Start `k` at `1` (since the first element at index `0` is always unique).
2.  Iterate `i` starting from `1` up to the end of the array.
3.  Compare `nums[i]` with the previous written unique element (or simply `nums[i-1]` in the original array context, but conceptually we are comparing `current` vs `previous`).
4.  If `nums[i]` is **different** from `nums[i-1]`, we have found a new unique number.
    *   Write it to `nums[k]`.
    *   Increment `k`.
5.  If `nums[i]` is the **same**, it's a duplicate. We ignore it and just increment `i`.
6.  Finally, return `k`, which represents the count of unique elements.

---

# Solution

## Java

```java
class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;
        
        int k = 1; // Index of the next unique element
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] != nums[i - 1]) {
                nums[k] = nums[i];
                k++;
            }
        }
        return k;
    }
}
```

## Go

```go
func removeDuplicates(nums []int) int {
    if len(nums) == 0 {
        return 0
    }
    
    k := 1
    for i := 1; i < len(nums); i++ {
        if nums[i] != nums[i-1] {
            nums[k] = nums[i]
            k++
        }
    }
    return k
}
```
