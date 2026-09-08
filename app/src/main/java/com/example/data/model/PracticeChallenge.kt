package com.example.data.model

data class PracticeChallenge(
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val description: String,
    val sampleInput: String,
    val sampleOutput: String,
    val defaultLanguage: Language,
    val starterCodes: Map<Language, String>
)

object PracticeDataset {
    val challenges = listOf(
        PracticeChallenge(
            id = "c1_reverse_string",
            title = "1. Reverse a String",
            category = "Strings & Arrays",
            difficulty = "Easy",
            description = "Write a function that takes an input string and returns the reversed string without using built-in reverse helpers.",
            sampleInput = "\"hello\"",
            sampleOutput = "\"olleh\"",
            defaultLanguage = Language.PYTHON,
            starterCodes = mapOf(
                Language.PYTHON to """def reverse_string(s: str) -> str:
    # TODO: Implement string reversal
    result = ""
    for char in s:
        result = char + result
    return result

test = "CodeFix"
print("Reversed:", reverse_string(test))
""",
                Language.JAVASCRIPT to """function reverseString(str) {
    // TODO: Reverse the string
    let reversed = "";
    for (let i = str.length - 1; i >= 0; i--) {
        reversed += str[i];
    }
    return reversed;
}

console.log(reverseString("CodeFix"));
""",
                Language.CPP to """#include <iostream>
#include <string>
using namespace std;

string reverseString(string str) {
    // TODO: Reverse the string
    int n = str.length();
    for (int i = 0; i < n / 2; i++) {
        swap(str[i], str[n - i - 1]);
    }
    return str;
}

int main() {
    cout << reverseString("CodeFix") << endl;
    return 0;
}
""",
                Language.C to """#include <stdio.h>
#include <string.h>

void reverseString(char str[]) {
    int n = strlen(str);
    for (int i = 0; i < n / 2; i++) {
        char temp = str[i];
        str[i] = str[n - i - 1];
        str[n - i - 1] = temp;
    }
}

int main() {
    char word[] = "CodeFix";
    reverseString(word);
    printf("Reversed: %s\\n", word);
    return 0;
}
""",
                Language.JAVA to """public class Main {
    public static String reverseString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(reverseString("CodeFix"));
    }
}
"""
            )
        ),
        PracticeChallenge(
            id = "c2_palindrome",
            title = "2. Palindrome Checker",
            category = "Logic & BCA Basics",
            difficulty = "Easy",
            description = "Check whether a given string or number reads the same backward as forward (ignoring non-alphanumeric chars).",
            sampleInput = "\"madam\"",
            sampleOutput = "true",
            defaultLanguage = Language.PYTHON,
            starterCodes = mapOf(
                Language.PYTHON to """def is_palindrome(text: str) -> bool:
    clean = "".join(c.lower() for c in text if c.isalnum())
    return clean == clean[::-1]

print("Is 'racecar' a palindrome?", is_palindrome("racecar"))
print("Is 'hello' a palindrome?", is_palindrome("hello"))
""",
                Language.CPP to """#include <iostream>
#include <string>
using namespace std;

bool isPalindrome(string s) {
    int l = 0, r = s.length() - 1;
    while (l < r) {
        if (s[l] != s[r]) return false;
        l++; r--;
    }
    return true;
}

int main() {
    cout << (isPalindrome("radar") ? "Palindrome" : "Not Palindrome") << endl;
    return 0;
}
"""
            )
        ),
        PracticeChallenge(
            id = "c3_two_sum",
            title = "3. Two Sum Problem",
            category = "Data Structures",
            difficulty = "Medium",
            description = "Given an array of integers and a target sum, find the indices of the two numbers that add up to target.",
            sampleInput = "nums = [2, 7, 11, 15], target = 9",
            sampleOutput = "[0, 1]",
            defaultLanguage = Language.PYTHON,
            starterCodes = mapOf(
                Language.PYTHON to """def two_sum(nums, target):
    lookup = {}
    for i, num in enumerate(nums):
        diff = target - num
        if diff in lookup:
            return [lookup[diff], i]
        lookup[num] = i
    return []

print(two_sum([2, 7, 11, 15], 9))
"""
            )
        ),
        PracticeChallenge(
            id = "c4_bubble_sort",
            title = "4. Bubble Sort Algorithm",
            category = "Algorithms",
            difficulty = "Easy",
            description = "Sort an unsorted array of numbers in ascending order using the classic Bubble Sort algorithm.",
            sampleInput = "[64, 34, 25, 12, 22, 11, 90]",
            sampleOutput = "[11, 12, 22, 25, 34, 64, 90]",
            defaultLanguage = Language.C,
            starterCodes = mapOf(
                Language.C to """#include <stdio.h>

void bubbleSort(int arr[], int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

int main() {
    int arr[] = {64, 34, 25, 12, 22, 11, 90};
    int n = sizeof(arr)/sizeof(arr[0]);
    bubbleSort(arr, n);
    
    printf("Sorted array: ");
    for (int i = 0; i < n; i++) printf("%d ", arr[i]);
    printf("\\n");
    return 0;
}
"""
            )
        ),
        PracticeChallenge(
            id = "c5_html_pricing",
            title = "5. Responsive Pricing Card",
            category = "Web Development",
            difficulty = "Easy",
            description = "Build a modern responsive card with a glowing badge, feature checklist, and CTA button using HTML & CSS.",
            sampleInput = "Clean layout with HTML5 semantics",
            sampleOutput = "Styled pricing tier card",
            defaultLanguage = Language.HTML,
            starterCodes = mapOf(
                Language.HTML to """<!DOCTYPE html>
<html>
<head>
<style>
  body { background: #0f172a; color: #fff; font-family: system-ui; display: flex; justify-content: center; padding: 20px; }
  .card { background: #1e293b; border: 1px solid #38bdf8; border-radius: 12px; padding: 24px; max-width: 300px; text-align: center; }
  .price { font-size: 32px; font-weight: bold; color: #38bdf8; margin: 12px 0; }
  .badge { background: rgba(56,189,248,0.2); color: #38bdf8; padding: 4px 8px; border-radius: 20px; font-size: 12px; }
  button { background: #38bdf8; color: #0f172a; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; width: 100%; margin-top: 16px; }
</style>
</head>
<body>
  <div class="card">
    <span class="badge">PRO PLAN</span>
    <div class="price">$19/mo</div>
    <p>Unlimited AI code analysis & sandboxed runs</p>
    <button>Start Coding</button>
  </div>
</body>
</html>
"""
            )
        )
    )
}
