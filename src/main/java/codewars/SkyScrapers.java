package codewars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SkyScrapers {

  private static final Random RANDOM = new Random();
  private static int[][][] lines;
  private static int[][] permutations;

  static int[][] solvePuzzle(int[] clues) {
    int n = clues.length / 4;
    lines = createLineIndices(n);
    List<Integer> values = IntStream.range(0, n).boxed().collect(Collectors.toList());
    permutations = calculatePermutations(n, values);
    var board = createRandomBoard(n);
    while (true) {
      int errors = calculateErrors(board, clues);
      if (errors == 0) {
        return board;
      }
      minimizeConflicts(board, errors, clues);
    }
  }

  private static void minimizeConflicts(int[][] board, int errors, int[] clues) {

    for (int[][] line : lines) {
      List<int[][]> neighbours = new ArrayList<>();
      for (int[] permutation : permutations) {
        int[][] neighbour = createBoard(board, line, permutation);
        neighbours.add(neighbour);
      }
      for (int[][] neighbour : neighbours) {
        printBoard(neighbour);
        System.out.println();

      }
      System.exit(0);
    }
  }

  private static void printBoard(int[][] board) {
    for (int i = 0; i < board.length; i++) {
      System.out.println(Arrays.toString(board[i]));
    }
  }

  private static int[][] createBoard(int[][] board, int[][] line, int[] permutation) {
    int[][] newBoard = copyBoard(board);
    for (int i = 0; i < line.length; i++) {
      int x = line[i][0];
      int y = line[i][1];
      board[y][x] = permutation[i];
    }
    return newBoard;
  }

  private static int[][] copyBoard(int[][] board) {
    int n = board.length;
    int[][] newBoard = new int[n][];
    for (int i = 0; i < n; i++) {
      int[] line = board[i];
      newBoard[i] = new int[n];
      System.arraycopy(line, 0, newBoard[i], 0, n);
    }
    return newBoard;
  }

  private static int[][][] createLineIndices(int n) {
    int[][][] indices = new int[n + n][n][2];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        indices[i][j][0] = j;
        indices[i][j][1] = i;
      }
    }
    for (int i = n; i < n + n; i++) {
      for (int j = 0; j < n; j++) {
        indices[i][j][0] = i - n;
        indices[i][j][1] = j;
      }
    }
    return indices;
  }

  private static int[][] calculatePermutations(int n, List<Integer> values) {
    ArrayList<int[]> permutationsList = new ArrayList<>();
    calculatePermutations(new int[n], 0, values, permutationsList);
    return permutationsList.toArray(new int[0][]);
  }

  private static void calculatePermutations(
      int[] permutation, int index, List<Integer> values, ArrayList<int[]> result) {
    if (index == permutation.length - 1) {
      permutation[index] = values.get(0);
      result.add(Arrays.copyOf(permutation, permutation.length));
    }
    for (int i = 0; i < values.size(); i++) {
      List<Integer> newValues = new ArrayList<>(values);
      Integer value = newValues.remove(i);
      permutation[index] = value;
      calculatePermutations(permutation, index + 1, newValues, result);
    }
  }

  private static int[][][] createNeighbours(int[][] solution) {
    int n = solution.length;
    var neighbours = new int[n][n][n];
    for (int i = 0; i < n; i++) {
      var neighbour = cloneArray(solution);
      shuffleArray(neighbour[i]);
      neighbours[i] = neighbour;
    }
    return neighbours;
  }

  private static int calculateErrors(int[][] solution, int[] clues) {
    if ("1".equals("1")) {
      return 1;
    }
    int n = solution.length;
    int errors = 0;
    for (int x = 0; x < n; x++) {
      var bitSet = new BitSet(n);
      for (int[] row : solution) {
        bitSet.set(row[x] - 1);
      }
      errors += n - bitSet.cardinality();
    }
    int clueIndex = 0;

    //Top to bottom
    for (int x = 0; x < n; x++) {
      int count = 0;
      int max = 0;
      int clue = clues[clueIndex++];
      if (clue == 0) {
        continue;
      }
      for (int y = 0; y < n; y++) {
        if (solution[y][x] > max) {
          count++;
          max = solution[y][x];
        }
      }
      int error = Math.abs(count - clue);
      errors += error;
    }

    //Right to left
    for (int y = 0; y < n; y++) {
      int count = 0;
      int max = 0;
      int clue = clues[clueIndex++];
      if (clue == 0) {
        continue;
      }
      for (int x = n - 1; x >= 0; x--) {
        if (solution[y][x] > max) {
          count++;
          max = solution[y][x];
        }
      }
      int error = Math.abs(count - clue);
      errors += error;
    }

    //Bottom to top
    for (int x = n - 1; x >= 0; x--) {
      int count = 0;
      int max = 0;
      int clue = clues[clueIndex++];
      if (clue == 0) {
        continue;
      }
      for (int y = n - 1; y >= 0; y--) {
        if (solution[y][x] > max) {
          count++;
          max = solution[y][x];
        }
      }
      int error = Math.abs(count - clue);
      errors += error;
    }

    //Left to right
    for (int y = n - 1; y >= 0; y--) {
      int count = 0;
      int max = 0;
      int clue = clues[clueIndex++];
      if (clue == 0) {
        continue;
      }
      for (int x = 0; x < n; x++) {
        if (solution[y][x] > max) {
          count++;
          max = solution[y][x];
        }
      }
      int error = Math.abs(count - clue);
      errors += error;
    }

    return errors;
  }

  private static int[][] createRandomBoard(int n) {
    var randomBoard = new int[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        randomBoard[i][j] = j + 1;
      }
      shuffleArray(randomBoard[i]);
    }
    return randomBoard;
  }

  private static void shuffleArray(int[] array) {
    for (int i = array.length - 1; i > 0; i--) {
      int index = RANDOM.nextInt(i + 1);
      swap(array, index, i);
    }
  }

  private static void swap(int[] array, int i, int j) {
    int temp = array[j];
    array[j] = array[i];
    array[i] = temp;
  }

  private static int[][] cloneArray(int[][] src) {
    int length = src.length;
    int[][] target = new int[length][src[0].length];
    for (int i = 0; i < length; i++) {
      System.arraycopy(src[i], 0, target[i], 0, src[i].length);
    }
    return target;
  }
}