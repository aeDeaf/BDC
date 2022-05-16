import * as React from 'react'
import {useEffect, useState} from 'react'
import {createTheme, Divider, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Paper, ThemeProvider, Typography} from "@mui/material";
import {Circle} from "@mui/icons-material";
import axios from "axios";

function getNodes(setNodes) {
    axios.get('http://localhost:8090/nodes/')
        .then(response => {
            if (response.data !== "") {
                const nodesData = response.data['nodes']
                const onlineNodes = nodesData.filter(node => node.status === 'ONLINE')
                const offlineNodes = nodesData.filter(node => node.status === 'OFFLINE')
                const nodes = {
                    'ONLINE': onlineNodes,
                    'OFFLINE': offlineNodes
                }
                setNodes(nodes)
            }
        })
}

export default function Nodes() {
    const [nodes, setNodes] = useState({
        'ONLINE': [],
        'OFFLINE': []
    })

    const theme = createTheme({
        palette: {
            online: {
                main: '#00ff00'
            },
            offline: {
                main: '#ff0000'
            }
        }
    })

    useEffect(() => {
        const id = setInterval(() => {
            getNodes(setNodes)
        }, 3000)
        return () => clearInterval(id)
    }, [])

    return (
        <Paper style={{minHeight: '100%', marginLeft: '2px', marginTop: '2px'}} variant='outlined'>
            <Typography variant='h5'>
                Компьютеры
            </Typography>
            {nodes.ONLINE.length > 0 ?
                <Typography variant='h6'>
                    Доступные:
                </Typography>
                : null}
            <List>
                {nodes.ONLINE.map(node =>
                    <ListItem>
                        <ListItemButton>
                            <ListItemIcon>
                                <ThemeProvider theme={theme}>
                                    <Circle color='online' sx={{width: '15px', height: '15px'}}/>
                                </ThemeProvider>
                            </ListItemIcon>
                            <ListItemText>
                                <Typography>{node['nodeName']}</Typography>
                            </ListItemText>
                        </ListItemButton>
                    </ListItem>
                )}
            </List>
            {nodes.ONLINE.length > 0 ? <Divider/> : null}
            {nodes.OFFLINE.length > 0 ?
                <Typography variant='h6'>
                    Не в сети:
                </Typography>
                : null
            }
            <List>
                {nodes.OFFLINE.map(node =>
                    <ListItem>
                        <ListItemButton disabled={true}>
                            <ListItemIcon>
                                <ThemeProvider theme={theme}>
                                    <Circle color='offline' sx={{width: '15px', height: '15px'}}/>
                                </ThemeProvider>
                            </ListItemIcon>
                            <ListItemText>
                                <Typography>{node['nodeName']}</Typography>
                            </ListItemText>
                        </ListItemButton>
                    </ListItem>
                )}
            </List>
        </Paper>
    )
}