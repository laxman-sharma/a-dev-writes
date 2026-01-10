package main

import "fmt"

func rotate(nums []int, k int) {
	n := len(nums)
	k %= n
	count := 0

	for start := 0; count < n; start++ {
		curr := start
		prev := nums[curr]

		for {
			next := (curr + k) % n
			temp := nums[next]

			nums[next] = prev
			prev = temp

			count++
			curr = next

			if start == curr {
				break
			}
		}
	}

}

func main() {

	arr := []int{1, 2, 3, 4, 5, 6, 7}

	rotate(arr, 3)
	fmt.Println(arr)

	arr2 := []int{-1, -100, 3, 99}
	rotate(arr2, 2)
	fmt.Println(arr2)
}
