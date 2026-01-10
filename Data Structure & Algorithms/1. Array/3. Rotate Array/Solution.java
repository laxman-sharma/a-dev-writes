class Solution {
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        k %= n;

        int count = 0;

        for ( int start = 0; count < n ; start++) {
            int prev = nums[start];
            int curr = start;

            do {
                int next = ( curr + k ) % n;
                int temp = nums[next];

                nums[next] = prev;
                prev = temp;

                curr = next;
                count++;

            } while( start != curr );
        }
        
    }

    public static void main(String[] args) {

        Solution solution = new Solution();

        int[] nums = new int[] {1, 2, 3, 4, 5, 6, 7};
        int k = 3;

        solution.rotate(nums, k);

        for(int i = 0; i < nums.length; i++) {
            System.out.print(nums[i] + " ");
        }
        System.out.println();


        nums = new int[] {-1,-100,3,99};
        k = 2;

        solution.rotate(nums, k);

        for(int i = 0; i < nums.length; i++) {
            System.out.print(nums[i] + " ");
        } 
    }
}