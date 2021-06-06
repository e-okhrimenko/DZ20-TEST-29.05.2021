import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.List;

public class Test {
    private final String PATH;
    private int quantity = 0;
    private String testName;
    private final ArrayList<Task> TASK = new ArrayList<>();
    private final ArrayList<ErrAns> ERRORS = new ArrayList<>();
    private Scanner scanner;

    public Test(String path) {
        this.PATH = path;
    }

    public void start() throws IOException {
        int realQuantity = 0;

        parseQuestions();
        System.out.println("\n" + testName);

        while (quantity != realQuantity++) {
            List<Integer> userKeys = userAnswers(realQuantity);
            analyzeAnswers(userKeys, realQuantity);
        }
        calcResult();
    }

    private void calcResult() {
        if (ERRORS.size() <= 0) {
            System.out.println("\nПоздравляю! Вы прошли тест, все ответы верны.");
        } else {
            System.out.print("\nУвы, Вы не прошли тест. Было допущенно " + ERRORS.size() + " ошибок."
                    + "\nЖелаете ознакомиться c ошибками? (1- Да / 2 - Нет): ");
            boolean statusOk = true;
            while (statusOk) {
                String stringIn = scanner.nextLine().trim().replaceAll(" ", "");
                switch (stringIn) {
                    case "1":
                        printErrors();
                        statusOk = false;
                        break;
                    case "2":
                        statusOk = false;
                        break;
                    default:
                        System.out.print("Сделайте корректный выбор: 1 - Желаете ознакомиться / 2 - Завершить тест: ");
                }
            }
        }
    }

    private void printErrors() {
        for (int i = 0; i < ERRORS.size(); i++) {
            System.out.println("\nОшибка " + (i + 1) + " из " + ERRORS.size() + ":");
            System.out.println("На " + ERRORS.get(i).NUM_QUESTION + " вопрос: " + ERRORS.get(i).QUESTION);
            System.out.println("Из предложенрных вариантов ответа:");
            for (int j = 0; j < TASK.get(ERRORS.get(i).NUM_QUESTION - 1).ANSWERS.size(); j++) {
                System.out.print(j + 1 + ")  " + TASK.get(ERRORS.get(i).NUM_QUESTION - 1).ANSWERS.get(j));
            }
            System.out.println("Вы ответили: ");
            for (int j = 0; j < ERRORS.get(i).USER_ANSWER.size(); j++) {
                System.out.print("- " + ERRORS.get(i).USER_ANSWER.get(j));
            }
            System.out.println("Правилбный ответ: " + TASK.get(ERRORS.get(i).NUM_QUESTION - 1).KEY.toString() + ":");
            for (int j = 0; j < ERRORS.get(i).ANSWER.size(); j++) {
                System.out.print("- " + ERRORS.get(i).ANSWER.get(j));
            }
        }
    }

    private void analyzeAnswers(List<Integer> userKeys, int realQuantity) {
        Collections.sort(TASK.get(realQuantity - 1).KEY);
        if (TASK.get(realQuantity - 1).KEY.equals(userKeys)) {
            return;
        }
        ArrayList<String> ANSWER = new ArrayList<>();
        ArrayList<String> USER_ANSWER = new ArrayList<>();
        for (int i = 0; i < TASK.get(realQuantity - 1).KEY.size(); i++) {
            ANSWER.add(TASK.get(realQuantity - 1).ANSWERS.get(TASK.get(realQuantity - 1).KEY.get(i) - 1));
        }
        for (Integer userKey : userKeys) {
            USER_ANSWER.add(TASK.get(realQuantity - 1).ANSWERS.get(userKey - 1));
        }
        ERRORS.add(new ErrAns(realQuantity, TASK.get(realQuantity - 1).QUESTION, ANSWER, USER_ANSWER));
    }

    private List<Integer> userAnswers(int realQuantity) {
        scanner = new Scanner(System.in);
        List<Integer> userKeys;
        System.out.println("\nВопрос " + realQuantity + " из " + quantity
                + ":\n" + TASK.get(realQuantity - 1).QUESTION
                + "\n\nВарианты ответа:");
        for (int i = 1; i < TASK.get(realQuantity - 1).ANSWERS.size() + 1; i++) {
            System.out.print(i + ") " + TASK.get(realQuantity - 1).ANSWERS.get(i - 1));
        }
        while (true) {
            System.out.print("\nУкажите правельные варианты ответов разделяя запятой (1,2,3,...): ");
            String stringIn = scanner.nextLine().trim().replaceAll(" ", "");
            String[] tempKeys = stringIn.split(",");
            userKeys = new ArrayList<>();
            try {
                if (tempKeys.length == 0) {
                    throw new NumberFormatException();
                }
                for (String tempKey : tempKeys) {
                    userKeys.add(Integer.parseInt(tempKey));
                    if (userKeys.get(userKeys.size() - 1) > TASK.get(realQuantity - 1).ANSWERS.size() || userKeys.get(userKeys.size() - 1) < 1) {
                        throw new NumberFormatException();
                    }
                }
                Collections.sort(userKeys);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ввели не верно. Введите правильно!");
            }
        }
        return userKeys;
    }

    private void parseQuestions() throws IOException {
        RandomAccessFile file = new RandomAccessFile(PATH, "r");
        String line;
        String question = null;
        List<Integer> key;
        ArrayList<String> answers;

        while ((line = file.readLine()) != null) {
            if (line.contains("@Description")) {
                this.testName = file.readLine();
                continue;
            }
            if (line.contains("@Question")) {
                quantity++;
                question = file.readLine();
                continue;
            }
            if (line.contains("@Options")) {
                key = new ArrayList<>();
                final String substring = line.substring((line.indexOf("@Key") + 5));
                if (line.contains("@OnlyOne")) {
                    key.add(Integer.parseInt(substring));
                }
                if (line.contains("@Multiple")) {
                    String[] tempKeys = substring.split(",");
                    for (String tempKey : tempKeys) {
                        key.add(Integer.parseInt(tempKey));
                    }
                }
                answers = new ArrayList<>();
                boolean bool = true;
                while (bool) {
                    if (!(line = file.readLine()).equals("")) {
                        if (file.getFilePointer() >= file.length()) {
                            answers.add(line + "\n");
                            break;
                        }
                        answers.add(line + "\n");
                    } else {
                        bool = false;
                    }
                }
                TASK.add(new Task(question, key, answers));
            }
        }
        file.close();
    }

    private static class Task {
        private final String QUESTION;
        private final List<Integer> KEY;
        private final ArrayList<String> ANSWERS;

        private Task(String question, List<Integer> key, ArrayList<String> answers) {
            this.QUESTION = question;
            this.KEY = key;
            this.ANSWERS = answers;
        }
    }

    private static class ErrAns {
        private final int NUM_QUESTION;
        private final String QUESTION;
        private final ArrayList<String> ANSWER;
        private final ArrayList<String> USER_ANSWER;

        public ErrAns(int NUM_QUESTION, String QUESTION, ArrayList<String> ANSWER, ArrayList<String> USER_ANSWER) {
            this.NUM_QUESTION = NUM_QUESTION;
            this.QUESTION = QUESTION;
            this.ANSWER = ANSWER;
            this.USER_ANSWER = USER_ANSWER;
        }
    }
}
