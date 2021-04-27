//This program leaks number of set bits in secret (n clusters: n is number of bits to represent secret)
public class leaks_n1s {

	public static void leaks_n1s(int secret, int guess) {
		int cnt = 0;
		int i = 0;
		while (i < 30) {
			if (secret % 2 == 1) {
				int tmp_guess = guess;
				while (tmp_guess > 0) {
					tmp_guess = tmp_guess >> 1;
					cnt++;
				}
			}
			secret = secret >> 1;
			i = i +1;
		}
//		System.out.println(cnt);
	}
//	public static void main(String[] args)
//	{
//		leaks_n1s(62,5);
//	}
}
