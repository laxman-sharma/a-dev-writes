import java.util.Set;
import java.util.HashSet;

class Solution {
    public boolean containsDuplicate(int[] nums) {
        Set<Integer> set = new HashSet<>();
        
        for(int num : nums) {
            if(set.contains(num)) {
                return true;
            }
            set.add(num);
        }

        return false;
    }

    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] nums = new int[]{1, 2, 3, 4, 5};
        System.out.println(sol.containsDuplicate(nums));

        nums = new int[]{1, 1, 1, 3, 3, 4, 3, 2, 4, 2};
        System.out.println(sol.containsDuplicate(nums));   
    }
}