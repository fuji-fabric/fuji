package io.github.sakurawald.fuji.core.config.migrator.version;

public class SemVerComparator {

    @SuppressWarnings("StringSplitter")
    public static int compareSemVer(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < 3; i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0; // equal
    }

}
