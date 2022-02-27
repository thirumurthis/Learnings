#!/bin/sh

# Script to read the list of subscription from the Azure
# compare the list with provided input

RESOURCEGRP=<name-of-resource-group>
INPUT_SUBSCRIPTION=(subscription1 subscription2)


for SUBSCRIPTIONS in `az account list --all --query [].name --ouput tsv`
do

  # Exclude the unesscary the subscription
  if [[ " ${INPUT_SUBSCRIPTION[*]} " == *" $SUBSCRIPTIONS "* ]]; then
    continue
  fi
  
  # To fetch the VM from az command
  for VMNAME in `az vm list -g $RESOURCEGRP --subscription $SUBSCRIPTIONS --query "[].name" --output tsv`
  do 
     VMDISK=$VMNAME-dsk
     for DISKNAME in `az disk list -g $RESOURCEGRP --subscription $SUBSCRIPTIONS --query "[?contains(name,'$VMDISK')].[name]" --output tsv | grep -v Premium | grep "^project[A-Za-z]*-node[0-9]*-dsk*"`
     do
        echo "az vm disk detach -g $RESOURCEGRP --vm-name $VMNAME -n $DISKNAME"
     done
  done
 echo "-- completed--"
done
