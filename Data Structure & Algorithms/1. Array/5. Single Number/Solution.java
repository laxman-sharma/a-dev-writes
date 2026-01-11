class Solution {
    public int singleNumber(int[] nums) {
        int ans = 0;

        for(int num : nums) {
            ans ^= num;
        }

        return ans;
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] nums = new int[]{2, 2, 1};
        System.out.println(sol.singleNumber(nums));

        nums = new int[]{4, 1, 2, 1, 2};
        System.out.println(sol.singleNumber(nums));   

        nums = new int[]{1};
        System.out.println(sol.singleNumber(nums));   
    }
}