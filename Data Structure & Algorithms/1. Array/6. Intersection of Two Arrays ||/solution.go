package main

import (
	"fmt"
	"sort"
)

func main() {
	fmt.Println(intersect([]int{2}, []int{1, 2, 2, 1}))
	fmt.Println(intersect([]int{4, 9, 5}, []int{9, 4, 9, 8, 4}))

	fmt.Println("Solution 2")
	fmt.Println(intersect([]int{1, 2, 2, 1}, []int{2}))
	fmt.Println(intersect([]int{4, 9, 5}, []int{9, 4, 9, 8, 4}))
}

func intersect(nums1 []int, nums2 []int) []int {
	if len(nums1) < len(nums2) {
		return intersect(nums2, nums1)
	}

	count := make(map[int]int)

	for _, num := range nums1 {
		count[num]++
	}

	k := 0

	for _, num := range nums2 {
		if count[num] > 0 {
			count[num]--
			nums1[k] = num
			k++
		}
	}

	return nums1[:k]
}

func intersect2(nums1 []int, nums2 []int) []int {
	sort.Ints(nums1)
	sort.Ints(nums2)

	var intersection []int

	for i, j := 0, 0; i < len(nums1) && j < len(nums2); {
		if nums1[i] == nums2[j] {
			intersection = append(intersection, nums1[i])
			i++
			j++
		} else if nums1[i] < nums2[j] {
			i++
		} else {
			j++
		}
	}

	return intersection
}
