public class BeerSong {
    public static void main(String[] args) {
        int beerNum = 99;
        String word = "bottles";

        while (beerNum > 0) {

        if (beerNum == 1) {
            word = "bottle";
        }

        if (beerNum > 0) {
            System.out.println(beerNum + " " + word + " of beer on the wall.");
            System.out.println(beerNum + " " + word + " of beer.");
            System.out.println("Take one down.");
            System.out.println("Pass it around.");
            beerNum = beerNum -1;
        }
    }
        // Изначально оно внутри while - но у нас while не выполняется при требуемом значении beerNum
        if (beerNum == 0) {
            System.out.println("No bottles of beer on the wall.");
        }
    }
}
