import * as React from 'react';
import {useContext, useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import {Add, MoreVert, PlayArrow, Stop} from "@mui/icons-material";
import {
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Fab,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Tooltip
} from "@mui/material";
import axios from "axios";

const OpenDialogContext = React.createContext({
    open: false,
    setIsDialogOpen: () => {
    }
})

function createData(containerName, imageName, statusName) {
    let status
    if (statusName === 'CREATED') {
        status = 'Создан'
    } else if (statusName === 'UP') {
        status = 'Запущен'
    } else if (statusName === 'EXITED') {
        status = 'Остановлен'
    }
    return {containerName, imageName, status, statusName};
}


function getDockerContainerInfos(state, setState) {
    const dockerContainersInfosUrl = 'http://localhost:8090/frontend/containers'
    axios
        .get(dockerContainersInfosUrl)
        .then(response => {
            const data = response.data['dockerContainerInfos']
            const r = []
            const newState = {...state}
            data.forEach(line => {
                const status = line['status']
                r.push(createData(line['name'], line['image'], status))
                Object.keys(newState.circular)
                    .forEach(name => {
                        if (name === line['name']) {
                            const circularData = newState.circular[name]
                            if (status !== circularData.prevStatus) {
                                newState.circular[name].inProgress = false
                            }
                        }
                    })
            })
            newState.rows = r
            setState(newState)
        })
}

function sendGetDockerContainerInfosRequest() {
    return axios
        .post('http://localhost:8090/frontend', {
            'nodeName': 'node2',
            'commandType': 6,
            'parameters': []
        })
}

function getStatusMessage() {
    const statusesURL = 'http://localhost:8090/frontend/status'
    axios
        .get(statusesURL)
        .then(response => {
            if (response.data !== '') {
                sendGetDockerContainerInfosRequest()
                    .then(() => console.log("Send docker container infos request"))
            }
        })
}

function sendRunCommand(state, setState, ev) {
    const [containerName, statusName] = ev.currentTarget.id.split(' ')
    const data = {
        "nodeName": "node2",
        "commandType": statusName !== 'UP' ? 2 : 3,
        "parameters": [
            {
                "name": "containerName",
                "value": containerName
            }
        ]
    }
    axios.post('http://localhost:8090/frontend', data)
        .then(() => {
            const newState = {...state}
            const prevStatus = state.rows.filter(row => row['containerName'] === containerName)[0].statusName
            newState.circular[containerName] = {
                inProgress: true,
                'prevStatus': prevStatus
            }
            setState(newState)
        })
}

export default function ContainerTable() {
    const [state, setState] = useState({
        rows: [],
        circular: {},
    })

    const [open, setIsDialogOpen] = useState(false)
    const value = {open, setIsDialogOpen}

    useEffect(() => {
        sendGetDockerContainerInfosRequest()
            .then(() => console.log("Send docker container infos request"))
        const id = setInterval(() => {
            getStatusMessage()
            getDockerContainerInfos(state, setState)
        }, 3000)
        return () => clearInterval(id)
    }, [])

    if (state.rows !== undefined) {
        return (
            <div style={{width: '75%', marginLeft: 'auto', marginRight: 'auto', marginTop: '10px'}}>
                <Typography variant='h6'>
                    Docker контейнеры
                </Typography>
                <TableContainer component={Paper}>
                    <Table aria-label="simple table">
                        <TableHead>
                            <TableRow>
                                <TableCell>Имя контейнера</TableCell>
                                <TableCell>Образ</TableCell>
                                <TableCell>Статус</TableCell>
                                <TableCell sx={{width: '100px'}}>Действия</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {state.rows.map((row) => (
                                <TableRow
                                    key={row.name}
                                    sx={{'&:last-child td, &:last-child th': {border: 0}}}
                                >
                                    <TableCell>{row.containerName}</TableCell>
                                    <TableCell>{row.imageName}</TableCell>
                                    <TableCell>
                                        {(state.circular[row.containerName] !== undefined && state.circular[row.containerName].inProgress) ?
                                            <CircularProgress/> : row.status}
                                    </TableCell>
                                    <TableCell>
                                        <Tooltip title={row.status === 'Остановлен' ? 'Запуск' : 'Остановка'}>
                                            <IconButton
                                                id={row.containerName + ' ' + row.statusName}
                                                onClick={(ev) => sendRunCommand(state, setState, ev)}
                                                disabled={state.circular[row.containerName] !== undefined && state.circular[row.containerName].inProgress}
                                            >
                                                {row.status === 'Остановлен' ?
                                                    <PlayArrow
                                                        color={state.circular[row.containerName] !== undefined && state.circular[row.containerName].inProgress ? 'disabled' : 'primary'}/>
                                                    :
                                                    <Stop
                                                        color={state.circular[row.containerName] !== undefined && state.circular[row.containerName].inProgress ? 'disabled' : 'primary'}/>
                                                }
                                            </IconButton>
                                        </Tooltip>
                                        <IconButton>
                                            <MoreVert/>
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
                <div style={{display: 'flex', justifyContent: 'right', marginTop: '20px', marginRight: '10px'}}>
                    <Tooltip title={'Добавить контейнер'}>
                        <Fab color='primary' onClick={() => setIsDialogOpen(true)}>
                            <Add/>
                        </Fab>
                    </Tooltip>
                </div>
                <OpenDialogContext.Provider value={value}>
                    <AddContainerDialog/>
                </OpenDialogContext.Provider>
            </div>
        );
    } else {
        return (
            <div style={{width: '75%', marginLeft: 'auto', marginRight: 'auto', marginTop: '10px'}}>
                <Typography variant='h6'>
                    Docker контейнеры
                </Typography>
            </div>
        )
    }

}

function AddContainerDialog() {
    const {open, setIsDialogOpen} = useContext(OpenDialogContext)

    const [value, setValue] = useState('gpu')

    const handleChange = (ev) => {
        setValue(ev.target.value)
    }

    const handleOk = (ev) => {
        sendCreateContainerRequest()
        setIsDialogOpen(false)
    }

    return (
        <Dialog open={open} fullWidth={true} onClose={() => setIsDialogOpen(false)}>
            <DialogTitle>Добавить контейнер</DialogTitle>
            <DialogContentText>

            </DialogContentText>
            <DialogContent style={{marginTop: '5px'}}>
                <FormControl fullWidth>
                    <InputLabel>Выберите тип:</InputLabel>
                    <Select
                        label='Выберите тип:'
                        fullWidth={true}
                        value={value}
                        onChange={handleChange}
                    >
                        <MenuItem value={'gpu'}>GPU версия</MenuItem>
                        <MenuItem value={'noGPU'}>Версия без GPU</MenuItem>
                    </Select>
                </FormControl>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => setIsDialogOpen(false)}>Отмена</Button>
                <Button onClick={handleOk}>Создать</Button>
            </DialogActions>
        </Dialog>
    )
}

function sendCreateContainerRequest() {
    const data = {
        "nodeName": "node2",
        "commandType": 1,
        "parameters": [
            {
                "name": "imageName",
                "value": 'git.jinr.ru:5005/nica/docker-images/bmn:latest'
            }
        ]
    }
    axios.post('http://localhost:8090/frontend', data)
        .then(() => {
            console.log('Send create container request')
        })
}
