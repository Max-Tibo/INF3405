import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class LoginModule {
	static String dbPath = System.getProperty("user.dir") + "\\UserCredentials_DB.txt";

	public static boolean connection() {
		boolean isUsername = false;
		boolean isLogin = false;
		int tryCounter = 0;
		try {
			while (!isLogin) {
				tryCounter++;
				Scanner scanCredentials = new Scanner(new File(dbPath));
				String[] userCredentials = null;
				System.out.println("Enter user crendetials");
				System.out.print("Username (only letters): ");

				Scanner sc = new Scanner(System.in);
				String inUsername = sc.nextLine();

				while (!inUsername.matches("[a-zA-Z]+")) { // Demande un d'entrer un username en lettre
					System.out.println("Invalid username");
					System.out.print("Username (only letters): ");
					inUsername = sc.nextLine();
				}

				while (scanCredentials.hasNextLine()) { // Scan la bd à la recherche du username entré
					userCredentials = scanCredentials.nextLine().toString().split("\\.");
					if (inUsername.equals(userCredentials[0])) {
						isUsername = true;
						isLogin = login(userCredentials);
						break;
					}
				}
				if (!isUsername) { // Demande de créer un nouveau compte si le username n'existe pas dans la bd
					System.out.println("This User does not exist");
					System.out.print("Create new User (y/n): ");
					String anwser = sc.nextLine();
					if (anwser.equals("y")) {
						createUser(inUsername);
					}
				}
				if (tryCounter > 4) {
					System.out.println("Exceeded the limit of connection attempt");
					break;
				}
			}
			return isLogin;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean login(String[] userCredentials) { // Demande un password et le confirme avec la bd
		Scanner sc = new Scanner(System.in);
		System.out.print("Password: ");
		String inPassword = sc.nextLine();
		if (inPassword.equals(userCredentials[1])) {
			System.out.println("User " + userCredentials[0] + " is connected");
			return true;
		} else {
			System.out.println("Wrong password");
			return false;
		}
	}

	private static void createUser(String inUsername) { // Création d'un compte utilisateur sauvegardé dans la bd
		Scanner sc = new Scanner(System.in);
		System.out.print("Choose password: ");
		String newPassword = sc.nextLine();
		String newUserCredentials = (inUsername + "." + newPassword);

		BufferedWriter outCredentials;
		try {
			outCredentials = new BufferedWriter(new FileWriter(dbPath, true));
			outCredentials.write(System.getProperty("line.separator"));
			outCredentials.append(newUserCredentials);
			outCredentials.close();
			System.out.println("New account created");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
