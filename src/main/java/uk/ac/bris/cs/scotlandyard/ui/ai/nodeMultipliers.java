package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.HashMap;

public class nodeMultipliers {

    public final HashMap<Integer, Double> nodeMultipliers = new HashMap<>();

    public nodeMultipliers() {
        Integer[] lowestTier = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 20, 21, 29, 30, 33, 42, 43, 56, 57, 73, 77, 91, 92, 93, 94, 95, 96, 107, 109, 119, 120, 121, 122, 123, 124, 136, 144, 162, 171, 175, 176, 177, 184, 185, 189, 190, 192, 194, 195, 197, 198, 199};
        Integer[] secondTier = new Integer[]{12, 19, 22, 23, 24, 25, 26, 27, 28, 31, 32, 34, 37, 38, 39, 41, 44, 45, 46, 47, 54, 55, 58, 59, 61, 71, 72, 74, 75, 76, 78, 90, 97, 98, 105, 106, 108, 110, 111, 130, 135, 137, 138, 139, 145, 146, 147, 148, 149, 150, 152, 153, 155, 156, 157, 159, 161, 163, 164, 165, 167, 168, 169, 170, 172, 173, 174, 178, 179, 180, 181, 182, 183, 186, 187, 188, 191, 193, 196};
        Integer[] thirdTier = new Integer[]{35, 36, 40, 48, 49, 50, 51, 52, 53, 60, 62, 63, 67, 69, 70, 79, 80, 88, 89, 99, 112, 114, 117, 125, 128, 129, 131, 132, 133, 140, 141, 142, 143, 151, 154, 158, 160, 166};
        Integer[] topTier = new Integer[]{64, 65, 66, 68, 81, 82, 83, 84, 85, 86, 87, 100, 101, 102, 103, 104, 113, 115, 116, 118, 126, 127, 134};
        for (Integer num : lowestTier) {
            nodeMultipliers.put(num, 0.7);
        }
        for(Integer num : secondTier) {
            nodeMultipliers.put(num, 0.8);
        }
        for(Integer num : thirdTier) {
            nodeMultipliers.put(num, 0.9);
        }
        for(Integer num : topTier) {
            nodeMultipliers.put(num, 1.0);
        }
    }
    //I have attempted to give each node a score, based on how close to the middle it is
    //view resources/annotated weight map.jpg to see what the map looks like

}
