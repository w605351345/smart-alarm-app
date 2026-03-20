//
//  ChallengeGenerator.swift
//  SmartAlarm
//
//  本地题目生成器（支持离线生成）
//

import Foundation

class ChallengeGenerator {
    static let shared = ChallengeGenerator()
    private let random = Int.random
    
    private init() {}
    
    /// 生成题目
    func generateQuestion(difficulty: ChallengeDifficulty, type: ChallengeType) -> ChallengeQuestion {
        switch type {
        case .math:
            return generateMathQuestion(difficulty: difficulty)
        case .memory:
            return generateMemoryQuestion(difficulty: difficulty)
        case .sequence:
            return generateSequenceQuestion(difficulty: difficulty)
        case .arithmetic:
            return generateArithmeticQuestion(difficulty: difficulty)
        }
    }
    
    // MARK: - 数学计算题
    
    private func generateMathQuestion(difficulty: ChallengeDifficulty) -> ChallengeQuestion {
        var question = ""
        var answer = ""
        
        switch difficulty {
        case .easy:
            // 10 以内加减法
            let num1 = Int.random(in: 1...10)
            let num2 = Int.random(in: 1...10)
            if Bool.random() {
                question = "\(num1) + \(num2) = ?"
                answer = "\(num1 + num2)"
            } else {
                let max = max(num1, num2)
                let min = min(num1, num2)
                question = "\(max) - \(min) = ?"
                answer = "\(max - min)"
            }
            
        case .medium:
            // 50 以内两步运算
            let num1 = Int.random(in: 10...30)
            let num2 = Int.random(in: 5...20)
            let num3 = Int.random(in: 1...10)
            let ops = ["+-", "-+", "++", "--"].randomElement() ?? "+-"
            
            if ops == "+-" {
                question = "\(num1) + \(num2) - \(num3) = ?"
                answer = "\(num1 + num2 - num3)"
            } else if ops == "-+" {
                question = "\(num1) - \(num2) + \(num3) = ?"
                answer = "\(num1 - num2 + num3)"
            } else if ops == "++" {
                question = "\(num1) + \(num2) + \(num3) = ?"
                answer = "\(num1 + num2 + num3)"
            } else {
                question = "\(num1) - \(num2) - \(num3) = ?"
                answer = "\(num1 - num2 - num3)"
            }
            
        case .hard:
            // 100 以内混合运算（含乘除）
            let opType = Int.random(in: 0...3)
            let num1 = Int.random(in: 20...50)
            let num2 = Int.random(in: 2...10)
            let num3 = Int.random(in: 1...10)
            
            switch opType {
            case 0:
                question = "\(num1) + \(num2) × \(num3) = ?"
                answer = "\(num1 + num2 * num3)"
            case 1:
                question = "\(num1) - \(num2) × \(num3) = ?"
                answer = "\(num1 - num2 * num3)"
            case 2:
                question = "\(num1) × \(num2) = ?"
                answer = "\(num1 * num2)"
            default:
                let dividend = num1 * num2
                question = "\(dividend) ÷ \(num2) = ?"
                answer = "\(num1)"
            }
        }
        
        return ChallengeQuestion(question: question, answer: answer, type: "math")
    }
    
    // MARK: - 记忆数字题
    
    private func generateMemoryQuestion(difficulty: ChallengeDifficulty) -> ChallengeQuestion {
        let digits: Int
        switch difficulty {
        case .easy: digits = 3
        case .medium: digits = 5
        case .hard: digits = 7
        }
        
        var number = ""
        for _ in 0..<digits {
            number.append("\(Int.random(in: 0...9))")
        }
        
        return ChallengeQuestion(
            question: "请记住这个数字：\(number)",
            answer: number,
            type: "memory",
            displayNumber: number
        )
    }
    
    // MARK: - 数字序列题
    
    private func generateSequenceQuestion(difficulty: ChallengeDifficulty) -> ChallengeQuestion {
        var question = ""
        var answer = ""
        
        switch difficulty {
        case .easy:
            // 等差数列
            let start = Int.random(in: 1...10)
            let diff = Int.random(in: 1...5)
            let seq = [start, start + diff, start + diff * 2, start + diff * 3]
            question = "找出规律：\(seq[0]), \(seq[1]), \(seq[2]), \(seq[3]), ?"
            answer = "\(start + diff * 4)"
            
        case .medium:
            if Bool.random() {
                // 等比数列
                let base = Int.random(in: 2...4)
                let seq = [base, base * base, base * base * base, base * base * base * base]
                question = "找出规律：\(seq[0]), \(seq[1]), \(seq[2]), \(seq[3]), ?"
                answer = "\(seq[3] * base)"
            } else {
                // 二级等差
                let start = Int.random(in: 1...5)
                let firstDiff = Int.random(in: 2...4)
                let secondDiff = Int.random(in: 1...2)
                let a = start
                let b = a + firstDiff
                let c = b + firstDiff + secondDiff
                let d = c + firstDiff + secondDiff * 2
                question = "找出规律：\(a), \(b), \(c), \(d), ?"
                answer = "\(d + firstDiff + secondDiff * 3)"
            }
            
        case .hard:
            // 斐波那契
            let f1 = Int.random(in: 1...3)
            let f2 = Int.random(in: 1...3)
            let f3 = f1 + f2
            let f4 = f2 + f3
            let f5 = f3 + f4
            question = "找出规律：\(f1), \(f2), \(f3), \(f4), \(f5), ?"
            answer = "\(f4 + f5)"
        }
        
        return ChallengeQuestion(question: question, answer: answer, type: "sequence")
    }
    
    // MARK: - 算术应用题
    
    private func generateArithmeticQuestion(difficulty: ChallengeDifficulty) -> ChallengeQuestion {
        var question = ""
        var answer = ""
        
        switch difficulty {
        case .easy:
            let apples = Int.random(in: 3...7)
            let oranges = Int.random(in: 2...6)
            question = "小明有\(apples)个苹果，又买了\(oranges)个橘子，一共有多少个水果？"
            answer = "\(apples + oranges)"
            
        case .medium:
            let books = Int.random(in: 5...14)
            let given = Int.random(in: 1...3)
            let bought = Int.random(in: 2...6)
            question = "书架上有\(books)本书，借给小红\(given)本，又买了\(bought)本，现在有多少本？"
            answer = "\(books - given + bought)"
            
        case .hard:
            let money = Int.random(in: 50...99)
            let spent1 = Int.random(in: 10...29)
            let spent2 = Int.random(in: 5...19)
            question = "你有\(money)元钱，买书花了\(spent1)元，买文具花了\(spent2)元，还剩多少元？"
            answer = "\(money - spent1 - spent2)"
        }
        
        return ChallengeQuestion(question: question, answer: answer, type: "arithmetic")
    }
}

/// 题目数据
struct ChallengeQuestion {
    let question: String
    let answer: String
    let type: String
    var displayNumber: String? // 用于记忆题显示
}
