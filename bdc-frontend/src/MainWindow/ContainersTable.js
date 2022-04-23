import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import {MoreVert, PlayArrow, Stop} from "@mui/icons-material";
import {Tooltip} from "@mui/material";

function createData(containerName, imageName, status) {
    return {containerName, imageName, status};
}

const rows = [
    createData('bmnroot0', 'bmnroot', 'Остановлен'),
    createData('bmnroot1', 'bmnroot', 'Запущен')
];

export default function ContainerTable() {
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
                        {rows.map((row) => (
                            <TableRow
                                key={row.name}
                                sx={{'&:last-child td, &:last-child th': {border: 0}}}
                            >
                                <TableCell>{row.containerName}</TableCell>
                                <TableCell>{row.imageName}</TableCell>
                                <TableCell>{row.status}</TableCell>
                                <TableCell>
                                    <Tooltip title={row.status === 'Остановлен' ? 'Запуск' : 'Остановка'}>
                                        <IconButton>
                                            {row.status === 'Остановлен' ? <PlayArrow color='primary'/> : <Stop color='primary'/>}

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
        </div>
    );
}
