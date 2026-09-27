package com.example.data.local

data class StarterProject(
    val name: String,
    val description: String,
    val files: Map<String, String> // filename to content
)

object DefaultProjects {

    val starterProjects = listOf(
        createCalculatorProject(),
        createSnakeGameProject(),
        createTodoListProject(),
        createPortfolioProject(),
        createWeatherAppProject(),
        createEmptyProject()
    )

    private fun createCalculatorProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Neon Calculator</title>
    <link rel="stylesheet" href="style.css">
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="calculator-card">
        <div class="header">
            <span class="logo">⚡ CALC.AI</span>
            <span class="mode-tag">DEG</span>
        </div>
        <div class="screen-container">
            <div id="previous-op" class="history-display"></div>
            <div id="current-op" class="main-display">0</div>
        </div>
        <div class="keypad">
            <button class="btn btn-action" onclick="clearAll()">AC</button>
            <button class="btn btn-action" onclick="deleteChar()">DEL</button>
            <button class="btn btn-operator" onclick="appendOperator('%')">%</button>
            <button class="btn btn-operator" onclick="appendOperator('/')">÷</button>

            <button class="btn" onclick="appendNumber('7')">7</button>
            <button class="btn" onclick="appendNumber('8')">8</button>
            <button class="btn" onclick="appendNumber('9')">9</button>
            <button class="btn btn-operator" onclick="appendOperator('*')">×</button>

            <button class="btn" onclick="appendNumber('4')">4</button>
            <button class="btn" onclick="appendNumber('5')">5</button>
            <button class="btn" onclick="appendNumber('6')">6</button>
            <button class="btn btn-operator" onclick="appendOperator('-')">-</button>

            <button class="btn" onclick="appendNumber('1')">1</button>
            <button class="btn" onclick="appendNumber('2')">2</button>
            <button class="btn" onclick="appendNumber('3')">3</button>
            <button class="btn btn-operator" onclick="appendOperator('+')">+</button>

            <button class="btn btn-zero" onclick="appendNumber('0')">0</button>
            <button class="btn" onclick="appendNumber('.')">.</button>
            <button class="btn btn-equals" onclick="compute()">=</button>
        </div>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    font-family: 'JetBrains Mono', monospace;
    -webkit-tap-highlight-color: transparent;
}

body {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: radial-gradient(circle at 50% 20%, #1e1b4b, #0f172a 70%);
    padding: 16px;
    color: #f8fafc;
}

.calculator-card {
    width: 100%;
    max-width: 360px;
    background: rgba(15, 23, 42, 0.85);
    backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 28px;
    padding: 24px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6), 0 0 40px rgba(0, 229, 255, 0.15);
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.logo {
    font-size: 0.85rem;
    font-weight: 700;
    letter-spacing: 2px;
    color: #00e5ff;
}

.mode-tag {
    font-size: 0.75rem;
    padding: 2px 8px;
    background: rgba(0, 229, 255, 0.1);
    color: #00e5ff;
    border-radius: 8px;
    border: 1px solid rgba(0, 229, 255, 0.3);
}

.screen-container {
    background: #090d16;
    border-radius: 18px;
    padding: 20px;
    text-align: right;
    margin-bottom: 24px;
    border: 1px solid rgba(255, 255, 255, 0.05);
    min-height: 100px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.history-display {
    color: #64748b;
    font-size: 0.95rem;
    height: 24px;
    overflow-x: auto;
}

.main-display {
    color: #f8fafc;
    font-size: 2.2rem;
    font-weight: 700;
    overflow-x: auto;
}

.keypad {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
}

.btn {
    border: none;
    outline: none;
    border-radius: 16px;
    padding: 16px 0;
    font-size: 1.25rem;
    font-weight: 600;
    cursor: pointer;
    background: #1e293b;
    color: #f1f5f9;
    transition: all 0.12s ease;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
}

.btn:active {
    transform: scale(0.95);
    filter: brightness(1.2);
}

.btn-action {
    background: rgba(244, 63, 94, 0.15);
    color: #f43f5e;
    border: 1px solid rgba(244, 63, 94, 0.3);
}

.btn-operator {
    background: rgba(0, 229, 255, 0.15);
    color: #00e5ff;
    border: 1px solid rgba(0, 229, 255, 0.3);
}

.btn-equals {
    background: linear-gradient(135deg, #00e5ff, #10b981);
    color: #0f172a;
    font-weight: 700;
    box-shadow: 0 0 20px rgba(0, 229, 255, 0.4);
}

.btn-zero {
    grid-column: span 2;
}
        """.trimIndent()

        val js = """
let currentInput = '0';
let previousInput = '';
let currentOperator = null;

const currentDisplay = document.getElementById('current-op');
const historyDisplay = document.getElementById('previous-op');

function updateDisplay() {
    currentDisplay.innerText = currentInput;
    if (currentOperator !== null) {
        historyDisplay.innerText = `${'$'}{previousInput} ${'$'}{currentOperator}`;
    } else {
        historyDisplay.innerText = '';
    }
}

function appendNumber(num) {
    if (num === '.' && currentInput.includes('.')) return;
    if (currentInput === '0' && num !== '.') {
        currentInput = num;
    } else {
        currentInput += num;
    }
    updateDisplay();
}

function appendOperator(op) {
    if (currentInput === '' && previousInput !== '') {
        currentOperator = op;
        updateDisplay();
        return;
    }
    if (currentOperator !== null) {
        compute();
    }
    currentOperator = op;
    previousInput = currentInput;
    currentInput = '';
    updateDisplay();
}

function compute() {
    let result;
    const prev = parseFloat(previousInput);
    const curr = parseFloat(currentInput);
    if (isNaN(prev) || isNaN(curr)) return;

    switch (currentOperator) {
        case '+': result = prev + curr; break;
        case '-': result = prev - curr; break;
        case '*': result = prev * curr; break;
        case '/': result = curr === 0 ? 'Error' : prev / curr; break;
        case '%': result = prev % curr; break;
        default: return;
    }

    currentInput = String(result);
    currentOperator = null;
    previousInput = '';
    updateDisplay();
}

function clearAll() {
    currentInput = '0';
    previousInput = '';
    currentOperator = null;
    updateDisplay();
}

function deleteChar() {
    if (currentInput.length > 1) {
        currentInput = currentInput.slice(0, -1);
    } else {
        currentInput = '0';
    }
    updateDisplay();
}
        """.trimIndent()

        return StarterProject(
            name = "Neon Calculator",
            description = "Sleek glassmorphic calculator with math operations, animations, and sound feedback.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }

    private fun createSnakeGameProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Retro Cyber Snake</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="game-container">
        <div class="score-bar">
            <div>SCORE: <span id="score">0</span></div>
            <div>HIGH: <span id="high-score">0</span></div>
        </div>
        <canvas id="gameCanvas" width="360" height="360"></canvas>
        <div class="controls-panel">
            <button id="start-btn" class="glow-btn">START GAME</button>
            <div class="dpad">
                <button class="d-btn up" onclick="handleDirection('UP')">▲</button>
                <div class="d-mid">
                    <button class="d-btn left" onclick="handleDirection('LEFT')">◀</button>
                    <button class="d-btn down" onclick="handleDirection('DOWN')">▼</button>
                    <button class="d-btn right" onclick="handleDirection('RIGHT')">▶</button>
                </div>
            </div>
        </div>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
* { margin: 0; padding: 0; box-sizing: border-box; font-family: monospace; }
body {
    background: #090d16;
    color: #00e5ff;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 12px;
}
.game-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    max-width: 400px;
    width: 100%;
}
.score-bar {
    width: 100%;
    display: flex;
    justify-content: space-between;
    font-size: 1.1rem;
    font-weight: bold;
    padding: 8px 16px;
    background: #151e32;
    border-radius: 12px;
    border: 1px solid rgba(0, 229, 255, 0.3);
}
canvas {
    background: #0f172a;
    border: 2px solid #00e5ff;
    border-radius: 16px;
    box-shadow: 0 0 25px rgba(0, 229, 255, 0.2);
    width: 100%;
    max-width: 360px;
    height: auto;
    aspect-ratio: 1/1;
}
.controls-panel {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    width: 100%;
}
.glow-btn {
    background: #10b981;
    color: #0f172a;
    font-weight: bold;
    border: none;
    padding: 10px 24px;
    border-radius: 12px;
    font-size: 1rem;
    cursor: pointer;
    box-shadow: 0 0 15px rgba(16, 185, 129, 0.4);
}
.dpad {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
}
.d-mid {
    display: flex;
    gap: 12px;
}
.d-btn {
    width: 54px;
    height: 54px;
    background: #1e293b;
    border: 1px solid #00e5ff;
    color: #00e5ff;
    font-size: 1.2rem;
    border-radius: 14px;
    cursor: pointer;
}
.d-btn:active { background: #00e5ff; color: #0f172a; }
        """.trimIndent()

        val js = """
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElem = document.getElementById('score');
const highScoreElem = document.getElementById('high-score');
const startBtn = document.getElementById('start-btn');

const gridSize = 18;
const tileCount = canvas.width / gridSize;

let snake = [{ x: 10, y: 10 }];
let food = { x: 5, y: 5 };
let dx = 1;
let dy = 0;
let score = 0;
let highScore = localStorage.getItem('snakeHighScore') || 0;
highScoreElem.innerText = highScore;

let gameLoop = null;
let isPlaying = false;

function generateFood() {
    food.x = Math.floor(Math.random() * tileCount);
    food.y = Math.floor(Math.random() * tileCount);
}

function handleDirection(dir) {
    if (dir === 'UP' && dy === 0) { dx = 0; dy = -1; }
    if (dir === 'DOWN' && dy === 0) { dx = 0; dy = 1; }
    if (dir === 'LEFT' && dx === 0) { dx = -1; dy = 0; }
    if (dir === 'RIGHT' && dx === 0) { dx = 1; dy = 0; }
}

window.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowUp') handleDirection('UP');
    if (e.key === 'ArrowDown') handleDirection('DOWN');
    if (e.key === 'ArrowLeft') handleDirection('LEFT');
    if (e.key === 'ArrowRight') handleDirection('RIGHT');
});

function draw() {
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Food
    ctx.fillStyle = '#f43f5e';
    ctx.beginPath();
    ctx.arc((food.x + 0.5) * gridSize, (food.y + 0.5) * gridSize, gridSize / 2 - 2, 0, Math.PI * 2);
    ctx.fill();

    // Snake
    snake.forEach((part, index) => {
        ctx.fillStyle = index === 0 ? '#00e5ff' : '#10b981';
        ctx.fillRect(part.x * gridSize + 1, part.y * gridSize + 1, gridSize - 2, gridSize - 2);
    });

    const head = { x: snake[0].x + dx, y: snake[0].y + dy };

    // Wall collision
    if (head.x < 0 || head.x >= tileCount || head.y < 0 || head.y >= tileCount) {
        gameOver();
        return;
    }

    // Body collision
    for (let part of snake) {
        if (part.x === head.x && part.y === head.y) {
            gameOver();
            return;
        }
    }

    snake.unshift(head);

    if (head.x === food.x && head.y === food.y) {
        score += 10;
        scoreElem.innerText = score;
        if (score > highScore) {
            highScore = score;
            highScoreElem.innerText = highScore;
            localStorage.setItem('snakeHighScore', highScore);
        }
        generateFood();
    } else {
        snake.pop();
    }
}

function gameOver() {
    clearInterval(gameLoop);
    isPlaying = false;
    startBtn.innerText = 'PLAY AGAIN';
    alert('Game Over! Your Score: ' + score);
}

startBtn.addEventListener('click', () => {
    snake = [{ x: 10, y: 10 }];
    dx = 1;
    dy = 0;
    score = 0;
    scoreElem.innerText = score;
    generateFood();
    if (gameLoop) clearInterval(gameLoop);
    gameLoop = setInterval(draw, 110);
    isPlaying = true;
    startBtn.innerText = 'RESTART';
});
        """.trimIndent()

        return StarterProject(
            name = "Cyber Snake Arcade",
            description = "Classic arcade snake game with HTML5 Canvas, responsive D-pad controls, and high scores.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }

    private fun createTodoListProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DevTask Studio</title>
    <link rel="stylesheet" href="style.css">
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="app-card">
        <header>
            <div>
                <h1>DevTasks</h1>
                <p id="date-display">Today's Focus</p>
            </div>
            <div class="progress-pill">
                <span id="completed-count">0</span>/<span id="total-count">0</span> Done
            </div>
        </header>

        <form id="task-form" onsubmit="addTask(event)">
            <input type="text" id="task-input" placeholder="What needs to be built?" required autocomplete="off">
            <button type="submit" class="add-btn">+</button>
        </form>

        <div class="filters">
            <button class="filter-btn active" onclick="setFilter('all')">All</button>
            <button class="filter-btn" onclick="setFilter('active')">Active</button>
            <button class="filter-btn" onclick="setFilter('completed')">Completed</button>
        </div>

        <ul id="task-list"></ul>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Plus Jakarta Sans', sans-serif; }
body {
    background: #0f172a;
    color: #f8fafc;
    min-height: 100vh;
    display: flex;
    justify-content: center;
    padding: 24px 16px;
}
.app-card {
    width: 100%;
    max-width: 440px;
    background: #1e293b;
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.08);
}
header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
header h1 { font-size: 1.6rem; color: #38bdf8; font-weight: 700; }
header p { font-size: 0.85rem; color: #94a3b8; }
.progress-pill {
    background: rgba(56, 189, 248, 0.15);
    color: #38bdf8;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
}
#task-form {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}
#task-input {
    flex: 1;
    background: #0f172a;
    border: 1px solid #334155;
    padding: 14px 16px;
    border-radius: 14px;
    color: #f8fafc;
    outline: none;
    font-size: 0.95rem;
}
#task-input:focus { border-color: #38bdf8; }
.add-btn {
    background: #38bdf8;
    color: #0f172a;
    border: none;
    border-radius: 14px;
    width: 48px;
    font-size: 1.5rem;
    font-weight: bold;
    cursor: pointer;
}
.filters {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
}
.filter-btn {
    background: transparent;
    border: 1px solid #334155;
    color: #94a3b8;
    padding: 6px 14px;
    border-radius: 10px;
    font-size: 0.8rem;
    cursor: pointer;
}
.filter-btn.active {
    background: #38bdf8;
    color: #0f172a;
    border-color: #38bdf8;
    font-weight: 600;
}
#task-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.task-item {
    display: flex;
    align-items: center;
    gap: 12px;
    background: #0f172a;
    padding: 14px;
    border-radius: 14px;
    border: 1px solid rgba(255, 255, 255, 0.04);
}
.task-item.completed span {
    text-decoration: line-through;
    color: #64748b;
}
.task-item span { flex: 1; font-size: 0.95rem; }
.delete-btn {
    background: transparent;
    border: none;
    color: #ef4444;
    cursor: pointer;
    font-size: 1.1rem;
}
        """.trimIndent()

        val js = """
let tasks = [
    { id: 1, text: "Explore Free Code AI features", completed: true },
    { id: 2, text: "Build an interactive web app", completed: false },
    { id: 3, text: "Download project as ZIP", completed: false }
];
let currentFilter = 'all';

function renderTasks() {
    const list = document.getElementById('task-list');
    list.innerHTML = '';

    const filtered = tasks.filter(t => {
        if (currentFilter === 'active') return !t.completed;
        if (currentFilter === 'completed') return t.completed;
        return true;
    });

    filtered.forEach(task => {
        const li = document.createElement('li');
        li.className = `task-item ${'$'}{task.completed ? 'completed' : ''}`;
        li.innerHTML = `
            <input type="checkbox" ${'$'}{task.completed ? 'checked' : ''} onchange="toggleTask(${'$'}{task.id})">
            <span>${'$'}{task.text}</span>
            <button class="delete-btn" onclick="deleteTask(${'$'}{task.id})">×</button>
        `;
        list.appendChild(li);
    });

    const completed = tasks.filter(t => t.completed).length;
    document.getElementById('completed-count').innerText = completed;
    document.getElementById('total-count').innerText = tasks.length;
}

function addTask(e) {
    e.preventDefault();
    const input = document.getElementById('task-input');
    const text = input.value.trim();
    if (!text) return;

    tasks.push({ id: Date.now(), text: text, completed: false });
    input.value = '';
    renderTasks();
}

function toggleTask(id) {
    tasks = tasks.map(t => t.id === id ? { ...t, completed: !t.completed } : t);
    renderTasks();
}

function deleteTask(id) {
    tasks = tasks.filter(t => t.id !== id);
    renderTasks();
}

function setFilter(f) {
    currentFilter = f;
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.toggle('active', btn.innerText.toLowerCase() === f);
    });
    renderTasks();
}

renderTasks();
        """.trimIndent()

        return StarterProject(
            name = "DevTask Manager",
            description = "Productivity to-do list with filters, task counters, and interactive checkmarks.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }

    private fun createPortfolioProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Developer Portfolio</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="wrapper">
        <nav class="nav">
            <div class="logo">&lt;Alex.Dev /&gt;</div>
            <button class="hire-btn" onclick="sayHello()">Hire Me</button>
        </nav>
        <section class="hero">
            <span class="badge">Full-Stack Engineer</span>
            <h1>Crafting digital experiences with AI & Code.</h1>
            <p>I build blazing-fast web applications, mobile interfaces, and AI automation tools.</p>
            <div class="tags">
                <span>HTML5 / CSS3</span>
                <span>JavaScript</span>
                <span>Kotlin</span>
                <span>Gemini AI</span>
            </div>
        </section>
        <section class="projects">
            <h2>Featured Projects</h2>
            <div class="grid">
                <div class="card">
                    <h3>Free Code AI</h3>
                    <p>Live coding environment with AI assistant and instant preview.</p>
                </div>
                <div class="card">
                    <h3>CloudCanvas</h3>
                    <p>Collaborative vector illustration tool for designers.</p>
                </div>
            </div>
        </section>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
* { margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
body { background: #0b0f19; color: #f1f5f9; padding: 20px; line-height: 1.6; }
.wrapper { max-width: 600px; margin: 0 auto; }
.nav { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.logo { font-size: 1.2rem; font-weight: 700; color: #00e5ff; }
.hire-btn { background: #10b981; color: #0b0f19; border: none; padding: 8px 18px; border-radius: 20px; font-weight: bold; cursor: pointer; }
.hero { margin-bottom: 40px; }
.badge { display: inline-block; background: rgba(0, 229, 255, 0.1); color: #00e5ff; padding: 4px 12px; border-radius: 12px; font-size: 0.8rem; margin-bottom: 12px; }
.hero h1 { font-size: 2rem; margin-bottom: 14px; line-height: 1.2; }
.hero p { color: #94a3b8; margin-bottom: 20px; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tags span { background: #1e293b; padding: 6px 12px; border-radius: 8px; font-size: 0.8rem; color: #cbd5e1; }
.projects h2 { font-size: 1.4rem; margin-bottom: 16px; color: #00e5ff; }
.grid { display: flex; flex-direction: column; gap: 14px; }
.card { background: #151e32; padding: 18px; border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.05); }
.card h3 { font-size: 1.1rem; margin-bottom: 6px; color: #f8fafc; }
.card p { font-size: 0.9rem; color: #94a3b8; }
        """.trimIndent()

        val js = """
function sayHello() {
    alert("Thanks for your interest! alex.developer@example.com");
}
console.log("Portfolio loaded successfully with Free Code AI.");
        """.trimIndent()

        return StarterProject(
            name = "Developer Portfolio",
            description = "Clean modern personal resume and portfolio website with project showcases.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }

    private fun createWeatherAppProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Weather Dashboard</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="weather-card">
        <div class="search-box">
            <input type="text" id="city-input" placeholder="Search city..." value="Tokyo">
            <button onclick="updateWeather()">🔍</button>
        </div>
        <div class="main-info">
            <div class="weather-icon">☀️</div>
            <div class="temp-display"><span id="temp">24</span>°C</div>
            <div id="city-name" class="city">Tokyo, Japan</div>
            <div id="condition" class="desc">Clear Sky & Gentle Breeze</div>
        </div>
        <div class="metrics">
            <div class="metric-item">
                <span class="label">Humidity</span>
                <span id="humidity" class="val">48%</span>
            </div>
            <div class="metric-item">
                <span class="label">Wind</span>
                <span id="wind" class="val">12 km/h</span>
            </div>
            <div class="metric-item">
                <span class="label">UV Index</span>
                <span id="uv" class="val">6 Mod</span>
            </div>
        </div>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
* { margin: 0; padding: 0; box-sizing: border-box; font-family: sans-serif; }
body { background: #0f172a; min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 16px; }
.weather-card { width: 100%; max-width: 360px; background: linear-gradient(180deg, #1e293b, #0b0f19); border-radius: 28px; padding: 24px; color: #f8fafc; border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 20px 40px rgba(0,0,0,0.5); }
.search-box { display: flex; gap: 8px; margin-bottom: 24px; }
.search-box input { flex: 1; background: #0f172a; border: 1px solid #334155; padding: 10px 16px; border-radius: 12px; color: #f8fafc; outline: none; }
.search-box button { background: #00e5ff; border: none; padding: 10px 16px; border-radius: 12px; cursor: pointer; }
.main-info { text-align: center; margin-bottom: 28px; }
.weather-icon { font-size: 3.5rem; margin-bottom: 8px; }
.temp-display { font-size: 3rem; font-weight: 700; color: #00e5ff; }
.city { font-size: 1.2rem; font-weight: 600; margin-top: 4px; }
.desc { color: #94a3b8; font-size: 0.9rem; margin-top: 4px; }
.metrics { display: flex; justify-content: space-between; background: rgba(255, 255, 255, 0.05); padding: 16px; border-radius: 16px; }
.metric-item { display: flex; flex-direction: column; align-items: center; }
.label { font-size: 0.75rem; color: #94a3b8; }
.val { font-size: 0.95rem; font-weight: 600; margin-top: 4px; }
        """.trimIndent()

        val js = """
const cityData = {
    "tokyo": { temp: 24, condition: "Clear Sky", icon: "☀️", humidity: "48%", wind: "12 km/h" },
    "london": { temp: 16, condition: "Light Rain", icon: "🌧️", humidity: "78%", wind: "19 km/h" },
    "new york": { temp: 21, condition: "Partly Cloudy", icon: "⛅", humidity: "55%", wind: "15 km/h" },
    "paris": { temp: 22, condition: "Sunny", icon: "🌤️", humidity: "50%", wind: "10 km/h" }
};

function updateWeather() {
    const input = document.getElementById('city-input').value.trim().toLowerCase();
    const data = cityData[input] || { temp: Math.floor(Math.random() * 15 + 15), condition: "Sunny", icon: "☀️", humidity: "52%", wind: "14 km/h" };
    document.getElementById('temp').innerText = data.temp;
    document.getElementById('city-name').innerText = input.charAt(0).toUpperCase() + input.slice(1);
    document.getElementById('condition').innerText = data.condition;
    document.querySelector('.weather-icon').innerText = data.icon;
    document.getElementById('humidity').innerText = data.humidity;
    document.getElementById('wind').innerText = data.wind;
}
        """.trimIndent()

        return StarterProject(
            name = "Weather Dashboard",
            description = "Interactive weather card with dynamic temperature, search, and condition metrics.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }

    private fun createEmptyProject(): StarterProject {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Web Project</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <h1>Hello, World!</h1>
        <p>Built with <strong>Free Code AI</strong>.</p>
        <button id="click-me">Click Me</button>
    </div>
    <script src="script.js"></script>
</body>
</html>
        """.trimIndent()

        val css = """
body {
    margin: 0;
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #0f172a;
    color: #f8fafc;
    font-family: system-ui, -apple-system, sans-serif;
}
.container {
    text-align: center;
    padding: 32px;
    background: #1e293b;
    border-radius: 16px;
    box-shadow: 0 10px 25px rgba(0,0,0,0.3);
}
h1 { color: #00e5ff; margin-bottom: 8px; }
button {
    margin-top: 16px;
    padding: 10px 20px;
    background: #10b981;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
}
        """.trimIndent()

        val js = """
document.getElementById('click-me').addEventListener('click', () => {
    alert('Congratulations! Your code runs live inside Free Code AI.');
});
        """.trimIndent()

        return StarterProject(
            name = "Empty Web Workspace",
            description = "Clean starter project with HTML5, CSS3, and JavaScript ready to code.",
            files = mapOf(
                "index.html" to html,
                "style.css" to css,
                "script.js" to js
            )
        )
    }
}
