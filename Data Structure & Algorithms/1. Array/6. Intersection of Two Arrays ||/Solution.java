import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

class Solution {
    public int[] intersect(int[] nums1, int[] nums2) {
        if(nums1.length < nums2.length) {
            return intersect(nums2, nums1);
        }
        Map<Integer, Integer> count = new HashMap<>();

        for (int num : nums1) {
            count.merge(num, 1, Integer::sum);
        }
        int k = 0;
        for(int num : nums2) {
            int cnt = count.getOrDefault(num, 0);
            if(cnt > 0) {
                nums1[k++] = num;
                count.merge(num, -1, Integer::sum);
            }
        }

        return Arrays.copyOfRange(nums1, 0, k);
        
    }

    public int[] intersect2(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);

        int i = 0, j = 0, k = 0;
        while(i < nums1.length && j < nums2.length) {
            if(nums1[i] == nums2[j]) {
                nums1[k++] = nums1[i];
                i++;
                j++;
            } else if (nums1[i] < nums2[j]) {
                i++;
            } else {
                j++;
            }
        }

        return Arrays.copyOfRange(nums1, 0, k);
        
    }

    public static void main(String[] ars) {
        Solution solution = new Solution();

        System.out.println(solution.intersect(
            new int[] {1, 2, 2, 1},
            new int[] {2, 2}
        ));

        System.out.println(solution.intersect(
            new int[] {4, 9, 5},
            new int[] {9, 4, 9, 8, 4}
        ));

        System.out.println("==== Second Approach ====");

        System.out.println(solution.intersect2(
            new int[] {1, 2, 2, 1},
            new int[] {2, 2}
        ));

        System.out.println(solution.intersect2(
            new int[] {4, 9, 5},
            new int[] {9, 4, 9, 8, 4}
        ));

    }
}