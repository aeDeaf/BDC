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
import {Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Fab, Menu, MenuItem, Tooltip} from "@mui/material";
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
    const dockerContainersInfosUrl = 'http://localhost:8090/frontend/containers?timestamp=' + new Date().getTime()
    axios
        .get(dockerContainersInfosUrl)
        .then(response => {
            const data = response.data['dockerContainerInfos']
            if (data !== []) {
                const r = []
                const newState = {...state}
                data.forEach(line => {
                    const status = line['status']
                    r.push(createData(line['name'], line['image'], status))
                })
                const keys = Object.keys(newState.circular)
                for (let i = 0; i < keys.length; i++) {
                    if (newState.circular[keys[i]].stop) {
                        setTimeout(() => {
                            const s = {...state}
                            s.circular[keys[i]].inProgress = false
                            s.circular[keys[i]].stop = false
                            setState(s)
                        }, 3000)
                    }
                }
                if (r !== []) {
                    newState.rows = r
                }
                setState(newState)
            }
        })
}

function sendGetDockerContainerInfosRequest() {
    return axios
        .post('http://localhost:8090/frontend?timestamp=' + new Date().getTime(), {
            'nodeName': 'node1',
            'commandType': 6,
            'parameters': []
        })
}

function getStatusMessage(state, setState, updateMode, setUpdateMode) {
    const statusesURL = 'http://localhost:8090/frontend/status?timestamp=' + new Date().getTime()
    axios
        .get(statusesURL)
        .then(response => {
            if (response.data !== '') {
                const containerName = response.data['message']
                const newState = {...state}
                newState.circular[containerName] = {
                    inProgress: true,
                    prevState: undefined,
                    stop: true
                }
                setState(newState)
                sendGetDockerContainerInfosRequest().then(() => console.log())
            }
        })

}

function sendRunCommand(state, setState, ev) {
    const [containerName, statusName] = ev.currentTarget.id.split(' ')
    const data = {
        "nodeName": "node1",
        "commandType": statusName !== 'UP' ? 2 : 3,
        "parameters": [
            {
                "name": "containerName",
                "value": containerName
            }
        ]
    }
    axios.post('http://localhost:8090/frontend?timestamp=' + new Date().getTime(), data)
        .then(() => {
            const newState = {...state}
            const prevStatus = state.rows.filter(row => row['containerName'] === containerName)[0].statusName
            newState.circular[containerName] = {
                inProgress: true,
                'prevStatus': prevStatus,
                stop: false
            }
            setState(newState)
        })
}

function sendUpdateCommand(state, setState, anchor, setUpdateMode) {
    const containerName = anchor.id
    const data = {
        "nodeName": "node1",
        "commandType": 5,
        "parameters": [
            {
                "name": "containerName",
                "value": containerName
            }
        ]
    }
    axios.post('http://localhost:8090/frontend?timestamp=' + new Date().getTime(), data)
        .then(() => {
            const newState = {...state}
            const prevStatus = state.rows.filter(row => row['containerName'] === containerName)[0].statusName
            newState.circular[containerName] = {
                inProgress: true,
                'prevStatus': prevStatus,
            }
            setState(newState)
            setUpdateMode(true)
        })
}

export default function ContainerTable() {
    const [state, setState] = useState({
        rows: [],
        circular: {},
    })

    const [open, setIsDialogOpen] = useState(false)
    const [anchorEl, setAnchorEl] = useState(null)
    const [updateMode, setUpdateMode] = useState(false)
    const value = {open, setIsDialogOpen}

    useEffect(() => {
        sendGetDockerContainerInfosRequest()
            .then(() => console.log("Send docker container infos request"))
        const id = setInterval(() => {
            getStatusMessage(state, setState, getUpdateMode, setUpdateMode)
            getDockerContainerInfos(state, setState)
        }, 3000)
        return () => clearInterval(id)
    }, [])

    const handleMenuOpenBtnClick = (ev) => {
        setAnchorEl(ev.currentTarget)
    }

    const handleMenuClose = () => {
        setAnchorEl(null)
    }

    const handleUpdateClick = () => {
        sendUpdateCommand(state, setState, anchorEl, setUpdateMode)
        handleMenuClose()
    }

    const getUpdateMode = () => {
        return updateMode
    }

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
                                        <IconButton id={row.containerName} onClick={handleMenuOpenBtnClick}>
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
                <Menu open={Boolean(anchorEl)} anchorEl={anchorEl} onClose={handleMenuClose}>
                    <MenuItem onClick={handleUpdateClick}>Обновить</MenuItem>
                </Menu>
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
            <DialogContent>
                <DialogContentText>
                    <Typography>Cоздать новый контейнер?</Typography>
                </DialogContentText>
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
        "nodeName": "node1",
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
