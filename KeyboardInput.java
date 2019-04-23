import java.util.Scanner;

public class KeyboardInput {
    private Scanner keyboardInput = new Scanner(System.in);

    public String inputString(String promt, boolean checkEmpty) {

        boolean stringIsEmpty = true;
        String str = "";
        do {
            System.out.print(promt);
            str = keyboardInput.nextLine().trim();
            if (checkEmpty) {
                stringIsEmpty = checkEmptyString(str);
            } else {
                stringIsEmpty = false;
            }
        } while (stringIsEmpty);

        return str;
    }

    private boolean checkEmptyString(String stringToCheck) {
        if (stringToCheck.length() == 0) {
            System.out.println("Name can't be empty");
            return true;
        } else {
            return stringToCheck.trim().length() == 0;
        }
    }
}
