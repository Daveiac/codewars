package codewars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SkyScrapers {

  private static final Random RANDOM = new Random();
  private static int[][] permutationsWithoutRepeats;
  private static int[][] validRows;
  private static List<Map<int[], Integer>> columnErrors;
  private static int N;

  static int[][] solvePuzzle(int[] clues) {
    N = clues.length / 4;
    permutationsWithoutRepeats = calculatePermutationsWithoutRepeats();
    validRows = calculateValidRows(clues);
    columnErrors = new ArrayList<>(N);
    IntStream.range(0, N).forEach(i -> columnErrors.add(new HashMap<>()));
    var board = createRandomBoardWithValidRows();
    while (true) {
      int errors = calculateErrors(board, clues);
      if (errors == 0) {
        return board;
      }
      board = minimizeConflicts(board, errors,clues);
    }
  }

  private static int[][] calculateValidRows(int[] clues) {
    List<List<Integer>> validRows = new ArrayList<>(N);
    for (int i = 0; i < N; i++) {
      validRows.add(new ArrayList<>());
    }
    for (int i = 0; i < permutationsWithoutRepeats.length; i++) {
      int[] permutation = permutationsWithoutRepeats[i];
      for (int j = 0; j < N; j++) {
        int forwards = clues[N * 4 - j - 1];
        int backwards = clues[N + j];
        int errors = calculateLineErrors(permutation, forwards, backwards);
        if (errors == 0) {
          validRows.get(j).add(i);
        }
      }
    }
    int[][] validRowsArray = new int[N][];
    for (int i = 0; i < N; i++) {
      validRowsArray[i] = validRows.get(i).stream().mapToInt(Integer::intValue).toArray();
    }
    return validRowsArray;
  }

  private static int calculateLineErrors(int[] line, int forwards, int backwards) {
    if (forwards == 0 && backwards == 0) {
      return 0;
    }
    int errors = 0;
    int maxF = 0;
    int countF = 0;
    int maxB = 0;
    int countB = 0;
    BitSet bitSet = new BitSet(N);
    for (int i = 0; i < N; i++) {
      int start = line[i];
      bitSet.set(start);
      if (forwards > 0 && start > maxF) {
        countF++;
        maxF = start;
      }
      int end = line[N - i - 1];
      if (backwards > 0 && end > maxB) {
        countB++;
        maxB = end;
      }
    }
    errors += Math.abs(forwards - countF);
    errors += Math.abs(backwards - countB);
    errors += N - bitSet.cardinality();
    return errors;
  }

  private static int[][] minimizeConflicts(int[][] board, int errors, int[] clues) {
    int minErrors = errors;
    List<int[][]> bestBoards = new ArrayList<>();
    for (int row = 0; row < N; row++) {
      for (int validRow : validRows[row]) {
        int[][] neighbour = createBoard(board, row, permutationsWithoutRepeats[validRow]);
        int neighbourErrors = calculateErrors(neighbour, clues);
        if (neighbourErrors < minErrors) {
          bestBoards.clear();
          bestBoards.add(neighbour);
          minErrors = neighbourErrors;
        } else if (neighbourErrors == minErrors) {
          bestBoards.add(neighbour);
        }
      }
    }
    int size = bestBoards.size();
    return bestBoards.get(RANDOM.nextInt(size));
  }

  public static int[][] tryAllCombinations(int[] clues, int[][] board, int row) {
    for (int validRow : validRows[row]) {
      int[][] newBoard = copyBoard(board);
      newBoard[row] = permutationsWithoutRepeats[validRow];
      if (row == N - 1) {
        int errors = calculateErrors(newBoard, clues);
        if (errors == 0) {
          return newBoard;
        }
      } else {
        int[][] newSolution = tryAllCombinations(clues, newBoard, row + 1);
        if (newSolution != null) {
          return newSolution;
        }
      }
    }
    return null;
  }

  private static void printBoard(int[][] board, int[] clues) {
    System.out.println("  " + Arrays.toString(clues) + "\n");
    System.out
        .println("  " + Arrays.toString(Arrays.copyOf(clues, N)).replaceAll("[\\[\\],]", " "));
    for (int i = 0; i < board.length; i++) {
      int[] ints = board[i];
      System.out.print(clues[N * 4 - i - 1] + " ");
      System.out.print(Arrays.toString(ints));
      System.out.println(" " + clues[N + i]);
    }
    var list = Arrays.stream(Arrays.copyOfRange(clues, N * 2, N * 3)).boxed()
        .collect(Collectors.toList());
    Collections.reverse(list);
    System.out.println("  " + list.toString().replaceAll("[\\[\\],]", " "));
    System.out.println();
  }

  private static int[][] createBoard(int[][] board, int row, int[] validRow) {
    int[][] newBoard = copyBoard(board);
    newBoard[row] = validRow;
    return newBoard;
  }

  private static int[][] copyBoard(int[][] board) {
    int[][] newBoard = new int[N][];
    for (int i = 0; i < N; i++) {
      int[] line = board[i];
      newBoard[i] = new int[N];
      System.arraycopy(line, 0, newBoard[i], 0, N);
    }
    return newBoard;
  }

  private static int[][] calculatePermutationsWithoutRepeats() {
    List<Integer> values = IntStream.rangeClosed(1, N).boxed().collect(Collectors.toList());
    ArrayList<int[]> permutationsList = new ArrayList<>();
    calculatePermutationsWithoutRepeats(new int[N], 0, values, permutationsList);
    return permutationsList.toArray(new int[0][]);
  }

  private static void calculatePermutationsWithoutRepeats(
      int[] permutation, int index, List<Integer> values, ArrayList<int[]> result) {
    if (index == permutation.length - 1) {
      permutation[index] = values.get(0);
      result.add(Arrays.copyOf(permutation, permutation.length));
    }
    for (int i = 0; i < values.size(); i++) {
      List<Integer> newValues = new ArrayList<>(values);
      Integer value = newValues.remove(i);
      permutation[index] = value;
      calculatePermutationsWithoutRepeats(permutation, index + 1, newValues, result);
    }
  }

  private static int calculateErrors(int[][] solution, int[] clues) {
    int errors = 0;
    for (int col = 0; col < N; col++) {
      int[] column = new int[N];
      for (int row = 0; row < N; row++) {
        column[row] = solution[row][col];
      }
      int forwards = clues[col];
      int backwards = clues[N * 3 - col - 1];
      int colErrors = columnErrors.get(col).computeIfAbsent(column,
          a -> calculateLineErrors(column, forwards, backwards));
      errors += colErrors;
    }
    return errors;
  }

  private static int[][] createRandomBoardWithValidRows() {
    var randomBoard = new int[N][N];
    for (int i = 0; i < N; i++) {
      int length = validRows[i].length;
      int validRow = validRows[i][RANDOM.nextInt(length)];
      randomBoard[i] = permutationsWithoutRepeats[validRow];
    }
    return randomBoard;
  }
}