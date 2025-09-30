# 🎯 IDE Configuration - One-Click Run

Your IDE is now configured to handle everything automatically! No more manual scripts needed.

## 🚀 How to Run (One-Click)

### **VS Code**
1. **Open Command Palette:** `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
2. **Select:** "Tasks: Run Task" → Choose:
   - `start-infrastructure` (for full Redis + MySQL)
   - `start-mysql-only` (for MySQL only)
3. **Run Application:** Press `F5` or use "Run and Debug" panel
4. **Select Configuration:**
   - "Bidding App (Full Infrastructure)" 
   - "Bidding App (IDE Only)"

**Note:** Maven PATH is automatically configured in the IDE environment.

### **IntelliJ IDEA**
1. **Open Run Configurations:** Click dropdown next to Run button
2. **Select:**
   - "Bidding App (Full Infrastructure)" - starts Redis + MySQL + App
   - "Bidding App (IDE Only)" - starts MySQL + App only
3. **Click Run Button** ▶️

**Note:** Maven PATH is automatically configured in the IDE environment.

### **Eclipse**
1. **Right-click project** → "Run As" → "Run Configurations"
2. **Create new Java Application:**
   - Main class: `com.example.bidding.BiddingWebSocketApplication`
   - VM arguments: `-Dspring.profiles.active=ide`
3. **Before Launch:** Add "Maven Build" task
   - Goal: `exec:exec@start-mysql`

## 🔧 What Each Configuration Does

### **Full Infrastructure**
- ✅ Starts Redis container
- ✅ Starts MySQL container  
- ✅ Runs application with centralized messaging
- ✅ Perfect for testing multiple instances

### **IDE Only**
- ✅ Starts MySQL container only
- ✅ Runs application with local messaging
- ✅ Faster startup, simpler setup
- ✅ Perfect for development

## 🎯 One-Click Commands

### **VS Code Shortcuts**
- `Ctrl+Shift+P` → "Tasks: Run Task" → `start-infrastructure`
- `F5` → Select "Bidding App (Full Infrastructure)"

### **IntelliJ IDEA Shortcuts**
- `Shift+F10` → Run selected configuration
- `Ctrl+Shift+F10` → Run current file

### **Eclipse Shortcuts**
- `Ctrl+F11` → Run last configuration
- `F11` → Debug last configuration

## 🐛 Troubleshooting

### **"Docker not found"**
- Install Docker Desktop
- Make sure Docker is running
- Restart IDE after Docker installation

### **"Port already in use"**
- Stop existing containers: `docker stop bidding-mysql bidding-redis`
- Or change ports in `application.properties`

### **"MySQL connection failed"**
- Wait 30 seconds for MySQL to fully start
- Check if container is running: `docker ps`
- Check logs: `docker logs bidding-mysql`

## 🎉 Success Indicators

When everything works, you'll see:
```
🔧 Checking infrastructure dependencies...
✅ MySQL detected on port 3306
✅ Redis detected on port 6379 - using centralized messaging
🚀 Application starting...
Started BiddingWebSocketApplication in 3.456 seconds
```

## 🌐 Access Your App

- **Main App:** http://localhost:8080
- **API Docs:** http://localhost:8080/swagger-ui.html
- **WebSocket:** ws://localhost:8080/ws

**That's it! Just click Run and everything starts automatically!** 🎯

## ⚠️ Troubleshooting

### **Multiple Instances**
If you see "Port 8080 is already in use" or multiple instances running:

1. **Stop all existing instances:**
   ```bash
   pkill -f BiddingWebSocketApplication
   ```

2. **Stop Docker services:**
   ```bash
   ./ide-run.sh stop
   ```

3. **Start fresh:**
   ```bash
   ./ide-run.sh mysql  # or ./ide-run.sh full
   ```

4. **Then run from IDE**

### **Maven Command Not Found**
If you see "command not found: mvn" in terminal:

1. **Use the helper script:**
   ```bash
   ./ide-run.sh mysql
   export PATH="$PWD/apache-maven-3.9.11/bin:$PATH"
   mvn spring-boot:run
   ```

2. **Or use IDE Run button** (recommended - PATH is auto-configured)
