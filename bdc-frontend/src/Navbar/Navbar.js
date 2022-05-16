import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import {Drawer, List, ListItem, ListItemIcon, ListItemText} from "@mui/material";
import {Assessment, Settings} from "@mui/icons-material";

export default function Navbar() {
    const [state, setState] = React.useState({
        menuOpen: false
    })

    const toggleMenuButton = (open) => (event) => {
        if (event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')) {
            return;
        }
        console.log('Toggle menu button:' + open)
        setState({menuOpen: open})
    }

    return (
        <Box>
            <AppBar position="static" sx={{height: '60px'}}>
                <Toolbar>
                    <IconButton
                        size="large"
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        onClick={toggleMenuButton(true)}
                    >
                        <MenuIcon/>
                    </IconButton>
                    <Typography variant="h6" component="div">
                        BmnRoot Docker Configurator
                    </Typography>
                </Toolbar>
            </AppBar>
            <Drawer
                anchor='left'
                open={state['menuOpen']}
                onClose={toggleMenuButton(false)}
            >
                <Box
                    sx={{width: 250}}
                    role='presentation'
                    onClick={toggleMenuButton(false)}
                    onKeyDown={toggleMenuButton(false)}
                >
                    <List>
                        <ListItem button key='MainScreen'>
                            <ListItemIcon>
                                <Assessment/>
                            </ListItemIcon>
                            <ListItemText primary={'Основной экран'}/>
                        </ListItem>
                        <ListItem button key='Settings'>
                            <ListItemIcon>
                                <Settings/>
                            </ListItemIcon>
                            <ListItemText primary={'Настройки'}/>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>
        </Box>
    );
}