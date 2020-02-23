
public class Main {
	public static void main(String[] args) throws Exception {
		CFG grammar = new CFG(args[0]);
		System.out.println(grammar);

		grammar.generateDerivesToLambdaSet();
	}
}
