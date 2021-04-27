package modpow;

public class SimplifiedRSA {
	
	public static long count = 0;

	public static int modPowFastKocherReduction(int num, int e, int m, int max_high) { 
		// computes num^e mod m

		int s = 1;
		int y = num;
		int res = 0;
		count = 0;

		int bound = (int) (Math.log(max_high + 1) / Math.log(2));
		
        int j=0;
		while (e > 0) {
			if (e % 2 == 1) {
				// res = (s * y) % m;
				// reduction:
				int tmp = s * y;
				if (tmp > m) {
					tmp = tmp - m;
					count++;
				}
				res = tmp % m;
				count++;
			} else {
				res = s;
				count++;
			}
			s = (res * res) % m; // squaring the base
			e /= 2;
			count++;
			j++;
			
				
		}
		return res;
	}

}
