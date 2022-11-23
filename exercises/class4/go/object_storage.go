package main

import (
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"strings"
)

var path = "/tmp/object_storage"
var table = make(map[string]string)

func main() {
	init_content()
	handleCommand()
}

func handleCommand() {
	cmd := flag.String("cmd", "", "cmd: display/put/get/delete")
	objectId := flag.String("key", "", "objectId")
	value := flag.String("value", "", "data for object")
	flag.Parse()
	if *cmd == "put" {
		put(*objectId, *value)
		return
	}
	if *cmd == "get" {
		get(*objectId)
		return
	}
	if *cmd == "delete" {
		deleteObject(*objectId)
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
	println("for read:\n	./main -cmd=read -key=object1")
	println("for delete:\n	./main -cmd=delete -key=object1")
	println("for write:\n	./main -cmd=put -key=object1 -value=hello")
	println("for write:\n	./main -cmd=put -key=object2 -value=world")

}

func get(key string) {
	println(table[key])
}
func deleteObject(key string) {
	delete(table, key)
	writeToFile()
	dispaly()
}

func put(key, value string) {
	table[key] = value
	writeToFile()
	dispaly()
}

func init_content() {
	data := loadFile()
	fillContent(data)
}
func dispaly() {
	data := loadFile()
	print(string(data))
}

func loadFile() []byte {
	table = make(map[string]string)
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
	if len(data) == 0 {
		return
	}
	str := string(data)
	if str == "" {
		return
	}
	records := strings.Split(str, "\n")
	for _, v := range records {
		if len(v) == 0 {
			continue
		}
		items := strings.Split(v, "=")
		table[items[0]] = items[1]
	}
}

func writeToFile() {
	file, err := os.OpenFile(path, os.O_RDWR|os.O_TRUNC, 0666)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	s := ""
	for k, v := range table {
		record := fmt.Sprintf("%s=%s\n", k, v)
		s = s + record
	}
	_, err = file.WriteString(s)
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	err = file.Sync()
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}
	err = file.Close()
	if err != nil {
		fmt.Printf("%v", err)
		os.Exit(1)
	}

}
