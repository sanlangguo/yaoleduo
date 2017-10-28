package test;

public class T_float {
	public static void main(String[] args) {
		String money = "0.00";
		String price = "0.53";
		int num = 7;
		double moneyA = Double.parseDouble(money)
				+ Double.parseDouble(price) * num;
		System.out.println(Double.parseDouble(money));
		System.out.println(Double.parseDouble(price));
		System.out.println(moneyA);
	}
}
