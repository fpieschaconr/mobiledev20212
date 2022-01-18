import { Component } from '@angular/core';

import { SQLite, SQLiteObject } from '@ionic-native/sqlite/ngx';
import { Platform } from '@ionic/angular';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {
  databaseObj: SQLiteObject;
  //model: { pid: number; Name: string; Url: string; Telephone: number; Email: string; Products: string; Classification: string;};
  model: any;
  row_data: any = [];
  readonly database_name: string = 'reto8db.db';
  readonly table_name: string = 'companies';
  searchName: String;
  searchClassification: String;

  readonly classificationsSearch: string[] = [
    '(All)',
    'Consultoría',
    'Desarrollo a la medida',
    'Fábrica de Software',
  ];
  readonly classifications: string[] = [
    'Consultoría',
    'Desarrollo a la medida',
    'Fábrica de Software',
  ];

  // Handle Update Row Operation
  to_update_item: number;

  confirmDeleteModal: boolean = false;
  showInsertRowModal: boolean = false;
  updateActive: boolean = false;
  showDetailsItem: boolean = false;

  constructor(private platform: Platform, private sqlite: SQLite) {
    this.platform
      .ready()
      .then(() => {
        this.createDB();
      })
      .catch((error) => {
        console.log(error);
      });
  }

  // Create DB if not there
  createDB() {
    this.sqlite
      .create({
        name: this.database_name,
        location: 'default',
      })
      .then((db: SQLiteObject) => {
        this.databaseObj = db;
        alert('reto8db Database Created!');
        this.createTable();
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }

  // Create table
  createTable() {
    this.databaseObj
      .executeSql(
        `
    CREATE TABLE IF NOT EXISTS ${this.table_name} (pid INTEGER PRIMARY KEY, Name varchar(255),
     Url varchar(255), Telephone INTEGER, Email varchar(255), Products varchar(255), Classification varchar(255))
    `,
        []
      )
      .then(() => {
        alert('Table Created!');
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }

  insertRowModal() {
    this.model = {};
    this.showInsertRowModal = true;
  }

  //Inset row in the table
  insertRow() {
    // Value should not be empty
    if (
      this.model.Name === undefined ||
      this.model.Name === null ||
      this.model.Name === ''
    ) {
      alert('Enter Name');
      return;
    }
    if (
      this.model.Url === undefined ||
      this.model.Url === null ||
      this.model.Url === ''
    ) {
      alert('Enter URL');
      return;
    }
    if (
      this.model.Telephone === undefined ||
      this.model.Telephone === null ||
      this.model.Telephone === '' ||
      !(this.model.Telephone > 0)
    ) {
      alert('Enter Telephone');
      return;
    }
    if (
      this.model.Email === undefined ||
      this.model.Email === null ||
      this.model.Email === ''
    ) {
      alert('Enter Email');
      return;
    }
    if (
      this.model.Products === undefined ||
      this.model.Products === null ||
      this.model.Products === ''
    ) {
      alert('Enter Products');
      return;
    }
    if (
      this.model.Classification === undefined ||
      this.model.Classification === null ||
      this.model.Classification === ''
    ) {
      alert('Enter Classification');
      return;
    }

    this.showInsertRowModal = false;
    this.databaseObj
      .executeSql(
        `
      INSERT INTO ${this.table_name} (Name, Url, Telephone, Email, Products, Classification) VALUES ('${this.model.Name}','${this.model.Url}', ${this.model.Telephone},
      '${this.model.Email}','${this.model.Products}','${this.model.Classification}')
    `,
        []
      )
      .then(() => {
        alert('Row Inserted!');
        this.getRows();
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }

  viewDetails(item) {
    this.model = item;
    this.showDetailsItem = true;
  }

  // Retrieve rows from table
  getRows() {
    var query = `SELECT * FROM ${this.table_name}`;
    if (
      this.searchName !== undefined &&
      this.searchName !== null &&
      this.searchName !== ''
    ) {
      query += ` WHERE Name LIKE '%${this.searchName}%'`;
    }
    if (
      this.searchClassification !== undefined &&
      this.searchClassification !== null &&
      this.searchClassification !== '' &&
      this.searchClassification.search('(All)') != 1
    ) {
      if (query.search('WHERE') == 1) {
        query += ` AND`;
      } else {
        query += ` WHERE`;
      }
      query += ` Classification LIKE '%${this.searchClassification}%'`;
    }
    this.databaseObj
      .executeSql(query, [])
      .then((res) => {
        this.row_data = [];
        if (res.rows.length > 0) {
          for (var i = 0; i < res.rows.length; i++) {
            //res.rows.item(i).updateActive = false;
            this.row_data.push(res.rows.item(i));
          }
        }
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }

  confirmDelete(item) {
    this.model = item;
    this.confirmDeleteModal = true;
  }

  // Delete single row
  deleteRow() {
    this.confirmDeleteModal = false;

    this.databaseObj
      .executeSql(
        `
      DELETE FROM ${this.table_name} WHERE pid = ${this.model.pid}
    `,
        []
      )
      .then((res) => {
        alert('Row Deleted!');
        this.getRows();
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }

  // Enable update mode and keep row data in a variable
  enableUpdate(item) {
    //this.to_update_item = this.row_data.indexOf(item);
    //this.row_data[this.to_update_item].updateActive = true;
    this.updateActive = true;
    this.model = item;
    this.showInsertRowModal = true;
  }

  // Update row with saved row id
  updateRow() {
    if (
      this.model.Name === undefined ||
      this.model.Name === null ||
      this.model.Name === ''
    ) {
      alert('Enter Name');
      return;
    }
    if (
      this.model.Url === undefined ||
      this.model.Url === null ||
      this.model.Url === ''
    ) {
      alert('Enter URL');
      return;
    }
    if (
      this.model.Telephone === undefined ||
      this.model.Telephone === null ||
      this.model.Telephone === '' ||
      !(this.model.Telephone > 0)
    ) {
      alert('Enter Telephone');
      return;
    }
    if (
      this.model.Email === undefined ||
      this.model.Email === null ||
      this.model.Email === ''
    ) {
      alert('Enter Email');
      return;
    }
    if (
      this.model.Products === undefined ||
      this.model.Products === null ||
      this.model.Products === ''
    ) {
      alert('Enter Products');
      return;
    }
    if (
      this.model.Classification === undefined ||
      this.model.Classification === null ||
      this.model.Classification === ''
    ) {
      alert('Enter Classification');
      return;
    }

    this.showInsertRowModal = false;
    this.databaseObj
      .executeSql(
        `
      UPDATE ${this.table_name}
      SET Name = '${this.model.Name}', Url = '${this.model.Url}', Telephone = ${this.model.Telephone},
      Email = '${this.model.Email}', Products = '${this.model.Products}', Classification = '${this.model.Classification}'
      WHERE pid = ${this.model.pid}
    `,
        []
      )
      .then(() => {
        alert('Row Updated!');
        //this.row_data[this.to_update_item].updateActive = false;
        this.updateActive = false;
        this.getRows();
      })
      .catch((e) => {
        alert('error ' + JSON.stringify(e));
      });
  }
}
