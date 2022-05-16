import * as React from 'react';
import {Grid} from "@mui/material";
import ContainerTable from "./ContainersTable";
import Nodes from "./Nodes";

export default function MainWindow() {
    return (
        <Grid container spacing={2} style={{height: 'calc(100vh - 60px)'}}>
            <Grid item xs={2}>
                <Nodes/>
            </Grid>
            <Grid item xs={10}>
                <ContainerTable/>
            </Grid>
        </Grid>
    )
}