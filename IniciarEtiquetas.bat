@echo off
title Servidor de Etiquetas da Cozinha
echo ====================================================
echo Iniciando o Sistema de Etiquetas de Cozinha...
echo ====================================================

:: Garante que o script esta rodando na mesma pasta dos arquivos
cd /d "%~dp0"

:: Compila os arquivos Java (caso tenha feito alguma alteracao)
javac Produto.java Main.java

:: Aguarda 1 segundo
timeout /t 1 /nobreak >nul

:: Abre a pagina no navegador padrao automaticamente
start http://localhost:8080

:: Executa a aplicacao Java
java Main

pause