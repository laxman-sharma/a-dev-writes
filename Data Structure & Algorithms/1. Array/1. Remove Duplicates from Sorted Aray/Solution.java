class Solution {
    public int removeDuplicates(int[] nums) {
        int k = 1;
        for(int i = 1 ; i < nums.length ; i++) {
            if(nums[i] != nums[i-1]) {
                k++;
                nums[k] = nums[i];
            }
        }       
        return k;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println(solution.removeDuplicates(new int[] {1,1,2}));
        System.out.println(solution.removeDuplicates(new int[] {0,0,1,1,1,2,2,3,3,4}));

    }
}