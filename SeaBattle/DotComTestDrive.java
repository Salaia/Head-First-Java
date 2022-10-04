import java.util.ArrayList;

public class DotComTestDrive {

    public static void main(String[] args) {

        DotCom dot = new DotCom();

        ArrayList<String> locations = new ArrayList<String>();
        locations.add("2");
        locations.add("2");
        locations.add("4");

        dot.setLocationCells(locations);

        String userGuess = "2";

        String result = dot.checkYourself(userGuess);

        String testResult = "Неудача";

        if (result.equals("Попал")) {
            testResult = "Пройден";
        }

        System.out.println(testResult);
    }
}