package main

import (
	"flag"
	"fmt"
	"io/ioutil"
	"os"
)

var path = "/tmp/block_storage"
var content [8][9]byte

func main() {
	init_content()
	handleCommand()
}

func handleCommand() {
	cmd := flag.String("cmd", "", "cmd: display/write/read/delete")
	row := flag.Int("row", 0, "row for index,range:[0,8]")
	col := flag.Int("col", 0, "column for index, range:[0,8]")
	data := flag.String("data", "", "data for read and write")
	flag.Parse()
	if *cmd == "read" {
		read(*row, *col)
		return
	}
	if *cmd == "write" {
		write(*row, *col, (*data)[0])
		return
	}
	if *cmd == "delete" {
		delete(*row, *col)
		return
	}
	if *cmd == "display" {
		dispaly()
		return
	}
	help()
}

func help() {
	println("usage:")
	println("for display:\n	./main -cmd=display")
	println("for read:\n	./main -cmd=read -row=1 -col=2")
	println("for delete:\n	./main -cmd=delete -row=1 -col=2")
	println("for write:\n	./main -cmd=write  -row=1 -col=2 -data=H")
	println("for write:\n	./main -cmd=write -row=1 -col=3 -data=o")
	println("for write:\n	./main -cmd=write  -row=1 -col=4 -data=u")

}

func delete(row int, col int) {
	content[row][col] = '*'
	writeToFile()
	dispaly()
}

func read(row int, col int) {
	println(string(content[row][col]))
}
func write(row int, col int, data byte) {
	content[row][col] = data
	writeToFile()
	dispaly()
}
func init_content() {
	i := 0
	j := 0
	for i < 8 {
		j = 0
		for j < 8 {
			content[i][j] = '*'
			j++
		}
		i++
	}
	i = 0
	for i < 8 {
		content[i][8] = '\n'
		i++
	}

	data := loadFile()
	fillContent(data)
}
func dispaly() {
	data := loadFile()
	print(string(data))
}

func loadFile() []byte {
	_, err := os.Stat(path)
	if err != nil {
		file, _ := os.Create(path)
		file.Close()
		writeToFile()
	}
	file, err := os.Open(path)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	data, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	file.Close()
	return data

}
func fillContent(data []byte) {
	i := 0
	for i < len(data) {
		r := int(i / 9)
		c := int(i % 9)
		content[r][c] = data[i]
		i++
	}
}

func writeToFile() {
	var tofill = make([]byte, 72)
	i := 0

	for i < 8 {
		j := 0
		for j < 9 {
			index := i*9 + j
			tofill[index] = content[i][j]
			j++
		}
		i++
	}
	file, err := os.OpenFile(path, os.O_RDWR|os.O_TRUNC, 0666)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	_, err = file.Write(tofill)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	file.Sync()
	file.Close()

}
