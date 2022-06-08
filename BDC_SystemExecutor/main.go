package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os/exec"
)

type RunnerCommand struct {
	SendTimestamp int64  `json:"sendTimestamp"`
	Command       string `json:"command"`
}

type RunnerCommandResponse struct {
	InitialMessageTimestamp int64  `json:"initialMessageTimestamp"`
	SendTimestamp           int64  `json:"sendTimestamp"`
	Result                  string `json:"result"`
}

func main() {
	client := http.Client{}
	for true {
		resp, err := client.Get("http://localhost:9090/command")
		if err != nil {
			fmt.Println(err)
		} else {
			defer resp.Body.Close()
			bodyBytes, _ := ioutil.ReadAll(resp.Body)
			body := string(bodyBytes[:])
			if body != "" {
				var command RunnerCommand
				err2 := json.Unmarshal([]byte(body), &command)
				if err2 == nil {
					execCommand := command.Command
					fmt.Println("Exec command: " + execCommand)
					cmd := exec.Command("bash", "-c", execCommand)
					stdout, err := cmd.Output()
					if err == nil {
						result := string(stdout)
						response := RunnerCommandResponse{command.SendTimestamp, 10, result}
						b, err := json.Marshal(response)
						if err == nil {
							_, err3 := client.Post("http://localhost:9090/command", "application/json", bytes.NewBuffer(b))
							if err3 != nil {
								fmt.Println("Can't send response")
							}
						}
					}
				}
			}

		}
	}
}
