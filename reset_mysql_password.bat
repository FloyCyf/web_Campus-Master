@echo off
echo 正在停止 MySQL 服务...
net stop MySQL80

echo 等待服务停止...
timeout /t 3 /nobreak >nul

echo 以跳过授权模式启动 MySQL...
start /b "" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --skip-grant-tables

echo 等待 MySQL 启动...
timeout /t 5 /nobreak >nul

echo 重置密码...
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'Zdx962436510！'; FLUSH PRIVILEGES;"

echo 停止跳过授权模式的 MySQL...
taskkill /f /im mysqld.exe

echo 等待进程结束...
timeout /t 2 /nobreak >nul

echo 重新启动 MySQL 服务...
net start MySQL80

echo 密码重置完成！
pause