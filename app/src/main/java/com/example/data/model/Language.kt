package com.example.data.model

enum class Language(
    val id: String,
    val displayName: String,
    val extension: String,
    val pistonName: String,
    val pistonVersion: String,
    val sampleCode: String
) {
    PYTHON(
        id = "python",
        displayName = "Python",
        extension = "py",
        pistonName = "python",
        pistonVersion = "3.10.0",
        sampleCode = """# Welcome to CodeFix AI!
# Write. Run. Fix. Learn.

def fibonacci(n):
    if n <= 0:
        return []
    elif n == 1:
        return [0]
    
    seq = [0, 1]
    while len(seq) < n:
        seq.append(seq[-1] + seq[-2])
    return seq

print("Fibonacci series (first 8 numbers):")
print(fibonacci(8))
"""
    ),
    JAVASCRIPT(
        id = "javascript",
        displayName = "JavaScript",
        extension = "js",
        pistonName = "javascript",
        pistonVersion = "18.15.0",
        sampleCode = """// Welcome to CodeFix AI
// JavaScript Playground

function isPalindrome(str) {
    const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, '');
    const reversed = cleaned.split('').reverse().join('');
    return cleaned === reversed;
}

const testWord = "racecar";
console.log("Is '" + testWord + "' a palindrome? ->", isPalindrome(testWord));
"""
    ),
    CPP(
        id = "cpp",
        displayName = "C++",
        extension = "cpp",
        pistonName = "c++",
        pistonVersion = "10.2.0",
        sampleCode = """#include <iostream>
#include <vector>

using namespace std;

int main() {
    cout << "=== CodeFix AI - C++ Workspace ===" << endl;
    vector<int> numbers = {10, 20, 30, 40, 50};
    
    int sum = 0;
    for (int num : numbers) {
        sum += num;
    }
    
    cout << "Sum of vector elements: " << sum << endl;
    return 0;
}
"""
    ),
    C(
        id = "c",
        displayName = "C",
        extension = "c",
        pistonName = "c",
        pistonVersion = "10.2.0",
        sampleCode = """#include <stdio.h>

int main() {
    printf("Welcome to CodeFix AI for C Programming!\\n");
    
    int a = 15;
    int b = 25;
    int sum = a + b;
    
    printf("Result: %d + %d = %d\\n", a, b, sum);
    return 0;
}
"""
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        extension = "java",
        pistonName = "java",
        pistonVersion = "15.0.2",
        sampleCode = """public class Main {
    public static void main(String[] args) {
        System.out.println("CodeFix AI: Java Console");
        
        String studentName = "Alex";
        int semester = 3;
        
        System.out.println("Student: " + studentName + " | Semester: " + semester);
        System.out.println("Ready to compile & analyze!");
    }
}
"""
    ),
    HTML(
        id = "html",
        displayName = "HTML",
        extension = "html",
        pistonName = "html",
        pistonVersion = "5.0.0",
        sampleCode = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>CodeFix AI Project</title>
    <style>
        body { font-family: sans-serif; background: #0f172a; color: #f8fafc; padding: 20px; }
        .card { background: #1e293b; padding: 16px; border-radius: 8px; border: 1px solid #38bdf8; }
        h1 { color: #38bdf8; }
    </style>
</head>
<body>
    <div class="card">
        <h1>CodeFix AI Preview</h1>
        <p>HTML & CSS rendered cleanly in the student sandbox.</p>
        <button onclick="alert('Hello from CodeFix!')">Click Me</button>
    </div>
</body>
</html>
"""
    ),
    CSS(
        id = "css",
        displayName = "CSS",
        extension = "css",
        pistonName = "css",
        pistonVersion = "3.0.0",
        sampleCode = """/* CodeFix AI Stylesheet */
:root {
    --primary-color: #38bdf8;
    --bg-dark: #0f172a;
    --text-light: #f8fafc;
}

body {
    background-color: var(--bg-dark);
    color: var(--text-light);
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    margin: 0;
    padding: 24px;
}

.code-badge {
    background: rgba(56, 189, 248, 0.15);
    color: var(--primary-color);
    padding: 4px 10px;
    border-radius: 4px;
    font-family: monospace;
}
"""
    );

    companion object {
        fun fromId(id: String): Language {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PYTHON
        }
    }
}
